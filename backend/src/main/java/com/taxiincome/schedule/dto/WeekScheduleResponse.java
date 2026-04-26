package com.taxiincome.schedule.dto;

import java.time.LocalDate;
import java.util.List;

public record WeekScheduleResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        List<ScheduleResponse> shifts) {
}
