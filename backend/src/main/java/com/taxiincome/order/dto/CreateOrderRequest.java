package com.taxiincome.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateOrderRequest(
        @NotNull(message = "Tiền đơn không được để trống")
        @Min(value = 0, message = "Tiền đơn không được âm")
        Long orderAmount,

        @Min(value = 0, message = "Tiền bo không được âm")
        Long tipAmount,

        @Min(value = 1, message = "Số tài chỉ được 1 hoặc 2")
        @Max(value = 2, message = "Số tài chỉ được 1 hoặc 2")
        Short taxiCount,

        BigDecimal feeRate,

        LocalDate orderDate,

        LocalTime orderTime,

        @Size(max = 1000, message = "Ghi chú tối đa 1000 ký tự")
        String note) {
}
