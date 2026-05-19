package com.taxiincome.order;

import com.taxiincome.common.ApiException;
import com.taxiincome.common.UserContext;
import com.taxiincome.order.dto.CreateOrderRequest;
import com.taxiincome.order.dto.OrderResponse;
import com.taxiincome.security.AccessTokenHasher;
import com.taxiincome.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserContext userContext;
    private final OrderCalculationService calculationService;
    private final Clock clock;
    private final TransactionTemplate writeTransaction;
    private final TransactionTemplate readTransaction;

    public OrderCommandService(OrderRepository orderRepository,
                               UserRepository userRepository,
                               UserContext userContext,
                               OrderCalculationService calculationService,
                               Clock clock,
                               PlatformTransactionManager transactionManager) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.userContext = userContext;
        this.calculationService = calculationService;
        this.clock = clock;
        this.writeTransaction = new TransactionTemplate(transactionManager);
        this.readTransaction = new TransactionTemplate(transactionManager);
        this.readTransaction.setReadOnly(true);
    }

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

        long orderAmount = req.orderAmount();
        long tipAmount = req.tipAmount() == null ? 0L : req.tipAmount();
        short taxiCount = req.taxiCount() == null ? (short) 1 : req.taxiCount();
        BigDecimal feeRate = req.feeRate() == null
                ? OrderCalculationService.DEFAULT_FEE_RATE : req.feeRate();
        String idempotencyHash = AccessTokenHasher.sha256Hex(userId + "::" + rawKey);
        String payloadHash = payloadHash(req, orderAmount, tipAmount, taxiCount, feeRate);
        Optional<Order> existing = orderRepository.findByUserIdAndIdempotencyKeyHash(
                userId, idempotencyHash);
        if (existing.isPresent()) {
            return replay(existing.get(), payloadHash);
        }

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
        order.setIdempotencyPayloadHash(payloadHash);

        try {
            Order saved = Objects.requireNonNull(
                    writeTransaction.execute(status -> orderRepository.saveAndFlush(order)));
            return new OrderCreateResult(true, OrderResponse.of(saved));
        } catch (DataIntegrityViolationException e) {
            OrderCreateResult replay = Objects.requireNonNull(readTransaction.execute(status ->
                    orderRepository.findByUserIdAndIdempotencyKeyHash(userId, idempotencyHash)
                            .map(existingOrder -> replay(existingOrder, payloadHash))
                            .orElseThrow(() -> e)));
            return replay;
        }
    }

    private static OrderCreateResult replay(Order existing, String payloadHash) {
        String existingPayloadHash = existing.getIdempotencyPayloadHash();
        if (existingPayloadHash == null || !existingPayloadHash.equals(payloadHash)) {
            throw ApiException.conflict(
                    "IDEMPOTENCY_CONFLICT",
                    "Idempotency-Key đã được dùng với nội dung đơn khác. Hãy tạo key mới cho đơn mới.");
        }
        return new OrderCreateResult(false, OrderResponse.of(existing));
    }

    private static String payloadHash(CreateOrderRequest req, long orderAmount, long tipAmount,
                                      short taxiCount, BigDecimal feeRate) {
        String canonical = String.join("\u001F",
                Long.toString(orderAmount),
                Long.toString(tipAmount),
                Short.toString(taxiCount),
                feeRate.stripTrailingZeros().toPlainString(),
                req.orderDate() == null ? "" : req.orderDate().toString(),
                req.orderTime() == null ? "" : req.orderTime().withNano(0).toString(),
                req.note() == null ? "" : req.note().trim());
        return AccessTokenHasher.sha256Hex(canonical);
    }
}
