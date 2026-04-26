package com.taxiincome.schedule.dto;

import com.taxiincome.schedule.ShiftType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateScheduleRequest(
        @NotNull(message = "workDate là bắt buộc")
        LocalDate workDate,

        @NotNull(message = "shiftType là bắt buộc")
        ShiftType shiftType) {
}
