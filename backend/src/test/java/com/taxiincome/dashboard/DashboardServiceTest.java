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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserContext userContext;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        when(userContext.requireUserId()).thenReturn(userId);
        when(orderRepository.aggregate(any(), any(), any())).thenReturn(OrderAggregate.empty());

        Clock clock = Clock.fixed(
                Instant.parse("2026-04-15T03:00:00Z"),
                ZoneId.of("Asia/Ho_Chi_Minh"));
        dashboardService = new DashboardService(
                orderRepository, new PeriodCalculator(), userContext, clock);
    }

    @Test
    void summary_today_reflects_server_clock() {
        DashboardResponse response = dashboardService.summary();

        assertThat(response.today()).isEqualTo(LocalDate.of(2026, 4, 15));
    }
}
