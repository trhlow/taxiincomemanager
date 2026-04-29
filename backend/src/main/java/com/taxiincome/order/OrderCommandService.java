package com.taxiincome.order;

import com.taxiincome.common.ApiException;
import com.taxiincome.common.UserContext;
import com.taxiincome.order.dto.CreateOrderRequest;
import com.taxiincome.order.dto.OrderResponse;
import com.taxiincome.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserContext userContext;
    private final OrderCalculationService calculationService;
    private final Clock clock;

    public OrderCommandService(OrderRepository orderRepository,
                               UserRepository userRepository,
                               UserContext userContext,
                               OrderCalculationService calculationService,
                               Clock clock) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.userContext = userContext;
        this.calculationService = calculationService;
        this.clock = clock;
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
        BigDecimal feeRate = req.feeRate() == null
                ? OrderCalculationService.DEFAULT_FEE_RATE : req.feeRate();

        OrderCalculationService.Calculation c = calculationService.calculate(
                orderAmount, tipAmount, taxiCount, feeRate);

        var now = LocalDateTime.now(clock);
        var orderDate = req.orderDate() == null ? now.toLocalDate() : req.orderDate();
        var orderTime = req.orderTime() == null ? now.toLocalTime().withNano(0) : req.orderTime();

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
}
