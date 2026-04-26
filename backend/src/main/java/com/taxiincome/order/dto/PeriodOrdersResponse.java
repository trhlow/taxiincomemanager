package com.taxiincome.order.dto;

import java.time.LocalDate;
import java.util.List;

public record PeriodOrdersResponse(
        int periodIndex,
        LocalDate periodStart,
        LocalDate periodEnd,
        int orderCount,
        long totalOrderAmount,
        long totalFee,
        long totalTip,
        long totalNet,
        List<OrderResponse> orders) {
}
