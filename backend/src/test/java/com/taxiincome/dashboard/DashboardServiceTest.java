package com.taxiincome.dashboard;

import com.taxiincome.common.UserContext;
import com.taxiincome.order.OrderAggregate;
import com.taxiincome.order.OrderRepository;
import com.taxiincome.order.PeriodCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserContext userContext;

    private DashboardService dashboardService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        when(userContext.requireUserId()).thenReturn(userId);

        Clock clock = Clock.fixed(
                Instant.parse("2026-04-15T03:00:00Z"),
                ZoneId.of("Asia/Ho_Chi_Minh"));
        dashboardService = new DashboardService(
                orderRepository, new PeriodCalculator(), userContext, clock);
    }

    @Test
    void summary_today_reflects_server_clock() {
        LocalDate today = LocalDate.of(2026, 4, 15);
        when(orderRepository.aggregate(eq(userId), eq(today), eq(today)))
                .thenReturn(OrderAggregate.empty());
        when(orderRepository.aggregate(eq(userId), eq(LocalDate.of(2026, 4, 11)), eq(LocalDate.of(2026, 4, 20))))
                .thenReturn(OrderAggregate.empty());
        when(orderRepository.aggregate(eq(userId), eq(LocalDate.of(2026, 4, 1)), eq(LocalDate.of(2026, 4, 30))))
                .thenReturn(OrderAggregate.empty());

        DashboardResponse response = dashboardService.summary();

        assertThat(response.today()).isEqualTo(today);
    }

    @Test
    void summary_mapsRepositoryAggregatesToTotals() {
        LocalDate today = LocalDate.of(2026, 4, 15);
        when(orderRepository.aggregate(eq(userId), eq(today), eq(today)))
                .thenReturn(new OrderAggregate(2, 200_000L, 60_000L, 10_000L, 150_000L, 140_000L, 2));
        when(orderRepository.aggregate(eq(userId), eq(LocalDate.of(2026, 4, 11)), eq(LocalDate.of(2026, 4, 20))))
                .thenReturn(new OrderAggregate(5, 500_000L, 150_000L, 20_000L, 370_000L, 350_000L, 4));
        when(orderRepository.aggregate(eq(userId), eq(LocalDate.of(2026, 4, 1)), eq(LocalDate.of(2026, 4, 30))))
                .thenReturn(new OrderAggregate(12, 1_200_000L, 360_000L, 50_000L, 890_000L, 850_000L, 10));

        DashboardResponse r = dashboardService.summary();

        assertThat(r.today()).isEqualTo(today);
        assertThat(r.todayTotalNet()).isEqualTo(140_000L);
        assertThat(r.todayOrderCount()).isEqualTo(2);

        assertThat(r.currentPeriod().totalNet()).isEqualTo(350_000L);
        assertThat(r.currentPeriod().orderCount()).isEqualTo(5);
        assertThat(r.currentPeriod().index()).isEqualTo(2);

        assertThat(r.currentMonth().totalNet()).isEqualTo(850_000L);
        assertThat(r.currentMonth().orderCount()).isEqualTo(12);

        assertThat(r.totalTip()).isEqualTo(50_000L);
        assertThat(r.totalFee()).isEqualTo(360_000L);
        assertThat(r.workingDaysMonth()).isEqualTo(10);
        assertThat(r.workingDaysCurrentPeriod()).isEqualTo(4);
    }
}
