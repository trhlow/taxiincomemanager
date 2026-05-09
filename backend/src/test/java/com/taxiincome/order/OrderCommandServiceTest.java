package com.taxiincome.order;

import com.taxiincome.common.ApiException;
import com.taxiincome.common.UserContext;
import com.taxiincome.order.dto.CreateOrderRequest;
import com.taxiincome.order.dto.OrderResponse;
import com.taxiincome.security.AccessTokenHasher;
import com.taxiincome.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserContext userContext;

    private final OrderCalculationService calculationService = new OrderCalculationService();

    Clock clock;

    OrderCommandService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-05-10T04:00:00Z"), ZONE);
        service = new OrderCommandService(
                orderRepository, userRepository, userContext, calculationService, clock);
    }

    @Test
    void create_whenIdempotencyKeyMissing_throws() {
        UUID userId = UUID.fromString("dddddddd-eeee-ffff-0000-111111111111");
        when(userContext.requireUserId()).thenReturn(userId);
        when(userRepository.existsById(userId)).thenReturn(true);

        CreateOrderRequest req = new CreateOrderRequest(
                100_000L, 0L, (short) 1, new BigDecimal("0.300"),
                LocalDate.of(2026, 5, 10), LocalTime.of(12, 0), null);

        assertThatThrownBy(() -> service.create(req, Optional.empty()))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo("MISSING_IDEMPOTENCY_KEY");
    }

    @Test
    void create_whenIdempotencyKeyBlank_throws() {
        UUID userId = UUID.fromString("eeeeeeee-ffff-0000-1111-222222222222");
        when(userContext.requireUserId()).thenReturn(userId);
        when(userRepository.existsById(userId)).thenReturn(true);

        CreateOrderRequest req = new CreateOrderRequest(
                100_000L, 0L, (short) 1, new BigDecimal("0.300"),
                LocalDate.of(2026, 5, 10), LocalTime.of(12, 0), null);

        assertThatThrownBy(() -> service.create(req, Optional.of("   \t")))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo("MISSING_IDEMPOTENCY_KEY");
    }

    @Test
    void create_whenIdempotencyKeyHitsExisting_skipsSave() {
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        String rawKey = "payment-retry-1";
        String hash = AccessTokenHasher.sha256Hex(userId + "::" + rawKey);

        when(userContext.requireUserId()).thenReturn(userId);
        when(userRepository.existsById(userId)).thenReturn(true);

        Order existing = minimalOrder(userId, LocalDate.of(2026, 5, 10));
        existing.setId(UUID.fromString("11111111-2222-3333-4444-555555555555"));
        existing.setIdempotencyKeyHash(hash);

        when(orderRepository.findByUserIdAndIdempotencyKeyHash(userId, hash))
                .thenReturn(Optional.of(existing));

        CreateOrderRequest req = new CreateOrderRequest(
                500_000L, 0L, (short) 1, new BigDecimal("0.300"),
                LocalDate.of(2026, 5, 10), LocalTime.of(12, 0), null);

        OrderCreateResult out = service.create(req, Optional.of(rawKey));

        assertThat(out.newlyCreated()).isFalse();
        assertThat(out.response().id()).isEqualTo(existing.getId());
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_whenNewWithIdempotencyKey_setsHashAndSaves() {
        UUID userId = UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffffffff");
        String rawKey = "new-key";
        String hash = AccessTokenHasher.sha256Hex(userId + "::" + rawKey);

        when(userContext.requireUserId()).thenReturn(userId);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(orderRepository.findByUserIdAndIdempotencyKeyHash(userId, hash))
                .thenReturn(Optional.empty());

        UUID savedId = UUID.fromString("99999999-aaaa-bbbb-cccc-dddddddddddd");
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(savedId);
            return o;
        });

        CreateOrderRequest req = new CreateOrderRequest(
                100_000L, 0L, (short) 1, new BigDecimal("0.300"),
                null, null, null);

        OrderCreateResult out = service.create(req, Optional.of(rawKey));

        assertThat(out.newlyCreated()).isTrue();
        assertThat(out.response().id()).isEqualTo(savedId);
        ArgumentCaptor<Order> cap = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).saveAndFlush(cap.capture());
        assertThat(cap.getValue().getIdempotencyKeyHash()).isEqualTo(hash);
    }

    private static Order minimalOrder(UUID userId, LocalDate date) {
        Order o = new Order();
        o.setUserId(userId);
        o.setOrderAmount(100_000L);
        o.setFeeRate(new BigDecimal("0.300"));
        o.setFeeAmount(30_000L);
        o.setTipAmount(0L);
        o.setTaxiCount((short) 1);
        o.setSubtotal(70_000L);
        o.setNetAmount(70_000L);
        o.setOrderDate(date);
        o.setOrderTime(LocalTime.of(8, 0));
        o.setSourceType(OrderSourceType.MANUAL);
        return o;
    }
}
