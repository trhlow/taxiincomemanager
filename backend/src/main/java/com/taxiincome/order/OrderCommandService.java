package com.taxiincome.order;

import com.taxiincome.common.ApiException;
import com.taxiincome.common.UserContext;
import com.taxiincome.order.dto.CreateOrderRequest;
import com.taxiincome.order.dto.OrderResponse;
import com.taxiincome.security.AccessTokenHasher;
import com.taxiincome.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
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
    public OrderCreateResult create(CreateOrderRequest req, Optional<String> idempotencyKeyHeader) {
        UUID userId = userContext.requireUserId();
        if (!userRepository.existsById(userId)) {
            throw ApiException.notFound("USER_NOT_FOUND", "Không tìm thấy user");
        }

        String rawKey = idempotencyKeyHeader.map(String::trim).orElse("");
        if (rawKey.isEmpty()) {
            throw ApiException.badRequest(
                    "MISSING_IDEMPOTENCY_KEY",
                    "Thiếu header Idempotency-Key (bắt buộc). Dùng cùng một giá trị khi retry để tránh tạo đơn trùng.");
        }
        String idempotencyHash = AccessTokenHasher.sha256Hex(userId + "::" + rawKey);
        Optional<Order> existing = orderRepository.findByUserIdAndIdempotencyKeyHash(
                userId, idempotencyHash);
        if (existing.isPresent()) {
            return new OrderCreateResult(false, OrderResponse.of(existing.get()));
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
        order.setIdempotencyKeyHash(idempotencyHash);

        try {
            Order saved = orderRepository.saveAndFlush(order);
            return new OrderCreateResult(true, OrderResponse.of(saved));
        } catch (DataIntegrityViolationException e) {
            OrderResponse replay = orderRepository.findByUserIdAndIdempotencyKeyHash(userId, idempotencyHash)
                    .map(OrderResponse::of)
                    .orElseThrow(() -> e);
            return new OrderCreateResult(false, replay);
        }
    }
}
