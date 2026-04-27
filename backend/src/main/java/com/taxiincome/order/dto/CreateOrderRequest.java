package com.taxiincome.order.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
        @Max(value = 100_000_000, message = "Tiền đơn quá lớn")
        Long orderAmount,

        @Min(value = 0, message = "Tiền bo không được âm")
        @Max(value = 20_000_000, message = "Tiền bo quá lớn")
        Long tipAmount,

        @Min(value = 1, message = "Số tài chỉ được 1 hoặc 2")
        @Max(value = 2, message = "Số tài chỉ được 1 hoặc 2")
        Short taxiCount,

        @DecimalMin(value = "0.0", inclusive = true, message = "Tỷ lệ cước phải >= 0")
        @DecimalMax(value = "1.0", inclusive = true, message = "Tỷ lệ cước phải <= 1")
        @Digits(integer = 1, fraction = 3, message = "Tỷ lệ cước tối đa 3 chữ số thập phân")
        BigDecimal feeRate,

        LocalDate orderDate,

        LocalTime orderTime,

        @Size(max = 1000, message = "Ghi chú tối đa 1000 ký tự")
        String note) {
}
