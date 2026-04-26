package com.taxiincome.schedule.dto;

import java.time.LocalDate;

public record WeekCheckResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        long morningCount,
        long eveningCount,
        int requiredMorning,
        int requiredEvening,
        boolean isComplete,
        String message) {
}
