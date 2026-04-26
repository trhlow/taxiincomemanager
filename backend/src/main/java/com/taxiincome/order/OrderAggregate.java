package com.taxiincome.order;

public record OrderAggregate(
        long orderCount,
        long totalOrderAmount,
        long totalFee,
        long totalTip,
        long totalSubtotal,
        long totalNet,
        long workingDays) {

    public static OrderAggregate empty() {
        return new OrderAggregate(0, 0, 0, 0, 0, 0, 0);
    }
}
