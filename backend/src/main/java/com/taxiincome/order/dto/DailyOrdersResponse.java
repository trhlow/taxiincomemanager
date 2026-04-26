package com.taxiincome.order.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyOrdersResponse(
        LocalDate date,
        int orderCount,
        long totalOrderAmount,
        long totalFee,
        long totalTip,
        long totalNet,
        List<OrderResponse> orders) {
}
