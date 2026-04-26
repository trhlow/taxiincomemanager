package com.taxiincome.schedule.dto;

import com.taxiincome.schedule.ShiftType;
import com.taxiincome.schedule.WorkSchedule;

import java.time.LocalDate;
import java.util.UUID;

public record ScheduleResponse(
        UUID id,
        LocalDate workDate,
        ShiftType shiftType) {

    public static ScheduleResponse of(WorkSchedule s) {
        return new ScheduleResponse(s.getId(), s.getWorkDate(), s.getShiftType());
    }
}
