package com.taxiincome.order.dto;

import com.taxiincome.order.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        long orderAmount,
        BigDecimal feeRate,
        long feeAmount,
        long tipAmount,
        short taxiCount,
        long subtotal,
        long netAmount,
        LocalDate orderDate,
        LocalTime orderTime,
        String sourceType,
        String note,
        OffsetDateTime createdAt) {

    public static OrderResponse of(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getOrderAmount(),
                o.getFeeRate(),
                o.getFeeAmount(),
                o.getTipAmount(),
                o.getTaxiCount(),
                o.getSubtotal(),
                o.getNetAmount(),
                o.getOrderDate(),
                o.getOrderTime(),
                o.getSourceType().name(),
                o.getNote(),
                o.getCreatedAt());
    }
}
