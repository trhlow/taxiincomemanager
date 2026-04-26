package com.taxiincome.dashboard;

import com.taxiincome.common.UserContext;
import com.taxiincome.order.OrderAggregate;
import com.taxiincome.order.OrderRepository;
import com.taxiincome.order.PeriodCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final PeriodCalculator periodCalculator;
    private final UserContext userContext;
    private final Clock clock;

    public DashboardService(OrderRepository orderRepository,
                            PeriodCalculator periodCalculator,
                            UserContext userContext,
                            Clock clock) {
        this.orderRepository = orderRepository;
        this.periodCalculator = periodCalculator;
        this.userContext = userContext;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardResponse summary() {
        UUID userId = userContext.requireUserId();
        LocalDate today = LocalDate.now(clock);

        OrderAggregate todayAgg = aggOrEmpty(orderRepository.aggregate(userId, today, today));

        PeriodCalculator.Period period = periodCalculator.currentPeriod(today);
        OrderAggregate periodAgg = aggOrEmpty(
                orderRepository.aggregate(userId, period.start(), period.endInclusive()));

        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate lastOfMonth = firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth());
        OrderAggregate monthAgg = aggOrEmpty(
                orderRepository.aggregate(userId, firstOfMonth, lastOfMonth));

        return new DashboardResponse(
                today,
                todayAgg.totalNet(),
                todayAgg.orderCount(),
                new DashboardResponse.PeriodSummary(
                        period.index(), period.start(), period.endInclusive(),
                        periodAgg.totalNet(), periodAgg.orderCount()),
                new DashboardResponse.MonthSummary(
                        today.getYear(), today.getMonthValue(),
                        monthAgg.totalNet(), monthAgg.orderCount()),
                monthAgg.totalTip(),
                monthAgg.totalFee(),
                monthAgg.workingDays(),
                periodAgg.workingDays());
    }

    private static OrderAggregate aggOrEmpty(OrderAggregate a) {
        return a == null ? OrderAggregate.empty() : a;
    }
}
