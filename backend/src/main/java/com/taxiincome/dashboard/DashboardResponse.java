package com.taxiincome.dashboard;

import java.time.LocalDate;

public record DashboardResponse(
        LocalDate today,
        long todayTotalNet,
        long todayOrderCount,
        PeriodSummary currentPeriod,
        MonthSummary currentMonth,
        long totalTip,
        long totalFee,
        long workingDaysMonth,
        long workingDaysCurrentPeriod) {

    public record PeriodSummary(
            int index,
            LocalDate start,
            LocalDate end,
            long totalNet,
            long orderCount) {
    }

    public record MonthSummary(
            int year,
            int month,
            long totalNet,
            long orderCount) {
    }
}
