package com.taxiincome.order;

import com.taxiincome.common.ApiException;
import com.taxiincome.common.MoneyUtils;
import com.taxiincome.common.UserContext;
import com.taxiincome.order.dto.CreateOrderRequest;
import com.taxiincome.order.dto.DailyOrdersResponse;
import com.taxiincome.order.dto.OrderResponse;
import com.taxiincome.order.dto.PeriodOrdersResponse;
import com.taxiincome.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    public static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.300");

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserContext userContext;
    private final PeriodCalculator periodCalculator;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        UserContext userContext,
                        PeriodCalculator periodCalculator,
                        Clock clock) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.userContext = userContext;
        this.periodCalculator = periodCalculator;
        this.clock = clock;
    }

    /** Pure calculation — exposed for unit testing. */
    public static Calculation calculate(long orderAmount, long tipAmount,
                                        short taxiCount, BigDecimal feeRate) {
        if (orderAmount < 0) {
            throw ApiException.badRequest("INVALID_AMOUNT", "Tiền đơn không được âm");
        }
        if (tipAmount < 0) {
            throw ApiException.badRequest("INVALID_AMOUNT", "Tiền bo không được âm");
        }
        if (taxiCount != 1 && taxiCount != 2) {
            throw ApiException.badRequest("INVALID_TAXI_COUNT", "Số tài chỉ được 1 hoặc 2");
        }
        if (feeRate == null || feeRate.signum() < 0 || feeRate.compareTo(BigDecimal.ONE) > 0) {
            throw ApiException.badRequest("INVALID_FEE_RATE", "Tỷ lệ cước phải trong [0, 1]");
        }
        long feeAmount = MoneyUtils.multiplyRate(orderAmount, feeRate);
        long subtotal = orderAmount - feeAmount + tipAmount;
        long netAmount = (taxiCount == 2) ? MoneyUtils.halfRoundUp(subtotal) : subtotal;
        return new Calculation(feeAmount, subtotal, netAmount);
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest req) {
        UUID userId = userContext.requireUserId();
        if (!userRepository.existsById(userId)) {
            throw ApiException.notFound("USER_NOT_FOUND", "Không tìm thấy user");
        }

        long orderAmount = req.orderAmount();
        long tipAmount = req.tipAmount() == null ? 0L : req.tipAmount();
        short taxiCount = req.taxiCount() == null ? (short) 1 : req.taxiCount();
        BigDecimal feeRate = req.feeRate() == null ? DEFAULT_FEE_RATE : req.feeRate();

        Calculation c = calculate(orderAmount, tipAmount, taxiCount, feeRate);

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate orderDate = req.orderDate() == null ? now.toLocalDate() : req.orderDate();
        LocalTime orderTime = req.orderTime() == null ? now.toLocalTime().withNano(0) : req.orderTime();

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setUserId(userId);
        order.setOrderAmount(orderAmount);
        order.setFeeRate(feeRate);
        order.setFeeAmount(c.feeAmount());
        order.setTipAmount(tipAmount);
        order.setTaxiCount(taxiCount);
        order.setSubtotal(c.subtotal());
        order.setNetAmount(c.netAmount());
        order.setOrderDate(orderDate);
        order.setOrderTime(orderTime);
        order.setSourceType(OrderSourceType.MANUAL);
        order.setNote(req.note() == null ? null : req.note().trim());

        Order saved = orderRepository.save(order);
        return OrderResponse.of(saved);
    }

    @Transactional(readOnly = true)
    public DailyOrdersResponse byDate(LocalDate date) {
        UUID userId = userContext.requireUserId();
        List<Order> orders = orderRepository
                .findByUserIdAndOrderDateOrderByOrderTimeAsc(userId, date);
        long totalAmount = orders.stream().mapToLong(Order::getOrderAmount).sum();
        long totalFee = orders.stream().mapToLong(Order::getFeeAmount).sum();
        long totalTip = orders.stream().mapToLong(Order::getTipAmount).sum();
        long totalNet = orders.stream().mapToLong(Order::getNetAmount).sum();
        return new DailyOrdersResponse(
                date,
                orders.size(),
                totalAmount, totalFee, totalTip, totalNet,
                orders.stream().map(OrderResponse::of).toList());
    }

    @Transactional(readOnly = true)
    public List<DailyOrdersResponse> monthly(int year, int month) {
        UUID userId = userContext.requireUserId();
        if (month < 1 || month > 12) {
            throw ApiException.badRequest("INVALID_MONTH", "Tháng phải trong khoảng 1-12");
        }
        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate last = first.withDayOfMonth(first.lengthOfMonth());
        List<Order> orders = orderRepository
                .findByUserIdAndOrderDateBetweenOrderByOrderDateAscOrderTimeAsc(userId, first, last);
        return groupByDate(orders);
    }

    @Transactional(readOnly = true)
    public PeriodOrdersResponse currentPeriod() {
        UUID userId = userContext.requireUserId();
        LocalDate today = LocalDate.now(clock);
        PeriodCalculator.Period period = periodCalculator.currentPeriod(today);
        List<Order> orders = orderRepository
                .findByUserIdAndOrderDateBetweenOrderByOrderDateAscOrderTimeAsc(
                        userId, period.start(), period.endInclusive());
        long totalAmount = orders.stream().mapToLong(Order::getOrderAmount).sum();
        long totalFee = orders.stream().mapToLong(Order::getFeeAmount).sum();
        long totalTip = orders.stream().mapToLong(Order::getTipAmount).sum();
        long totalNet = orders.stream().mapToLong(Order::getNetAmount).sum();
        return new PeriodOrdersResponse(
                period.index(), period.start(), period.endInclusive(),
                orders.size(),
                totalAmount, totalFee, totalTip, totalNet,
                orders.stream().map(OrderResponse::of).toList());
    }

    private static List<DailyOrdersResponse> groupByDate(List<Order> orders) {
        return orders.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Order::getOrderDate,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()))
                .entrySet().stream()
                .map(e -> {
                    List<Order> os = e.getValue();
                    long ta = os.stream().mapToLong(Order::getOrderAmount).sum();
                    long tf = os.stream().mapToLong(Order::getFeeAmount).sum();
                    long tt = os.stream().mapToLong(Order::getTipAmount).sum();
                    long tn = os.stream().mapToLong(Order::getNetAmount).sum();
                    return new DailyOrdersResponse(
                            e.getKey(),
                            os.size(),
                            ta, tf, tt, tn,
                            os.stream().map(OrderResponse::of).toList());
                })
                .toList();
    }

    public record Calculation(long feeAmount, long subtotal, long netAmount) {
    }
}
