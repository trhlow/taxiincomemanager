package com.taxiincome.order;

import com.taxiincome.common.ApiException;
import com.taxiincome.common.UserContext;
import com.taxiincome.order.dto.DailyOrdersResponse;
import com.taxiincome.order.dto.OrderResponse;
import com.taxiincome.order.dto.PeriodOrdersResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final UserContext userContext;
    private final PeriodCalculator periodCalculator;
    private final Clock clock;

    public OrderQueryService(OrderRepository orderRepository,
                             UserContext userContext,
                             PeriodCalculator periodCalculator,
                             Clock clock) {
        this.orderRepository = orderRepository;
        this.userContext = userContext;
        this.periodCalculator = periodCalculator;
        this.clock = clock;
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
}
