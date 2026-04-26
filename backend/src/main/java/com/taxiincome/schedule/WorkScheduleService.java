package com.taxiincome.schedule;

import com.taxiincome.common.UserContext;
import com.taxiincome.schedule.dto.CreateScheduleRequest;
import com.taxiincome.schedule.dto.ScheduleResponse;
import com.taxiincome.schedule.dto.WeekCheckResponse;
import com.taxiincome.schedule.dto.WeekScheduleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class WorkScheduleService {

    public static final int REQUIRED_MORNING = 1;
    public static final int REQUIRED_EVENING = 2;

    private final WorkScheduleRepository repository;
    private final UserContext userContext;
    private final Clock clock;

    public WorkScheduleService(WorkScheduleRepository repository,
                               UserContext userContext,
                               Clock clock) {
        this.repository = repository;
        this.userContext = userContext;
        this.clock = clock;
    }

    @Transactional
    public ScheduleResponse upsert(CreateScheduleRequest req) {
        UUID userId = userContext.requireUserId();
        return repository
                .findByUserIdAndWorkDateAndShiftType(userId, req.workDate(), req.shiftType())
                .map(ScheduleResponse::of)
                .orElseGet(() -> {
                    WorkSchedule s = new WorkSchedule();
                    s.setId(UUID.randomUUID());
                    s.setUserId(userId);
                    s.setWorkDate(req.workDate());
                    s.setShiftType(req.shiftType());
                    return ScheduleResponse.of(repository.save(s));
                });
    }

    @Transactional
    public boolean delete(LocalDate workDate, ShiftType shiftType) {
        UUID userId = userContext.requireUserId();
        return repository.deleteByUserAndDateAndShift(userId, workDate, shiftType) > 0;
    }

    @Transactional(readOnly = true)
    public WeekScheduleResponse week(LocalDate anchor) {
        UUID userId = userContext.requireUserId();
        LocalDate weekStart = mondayOf(anchor == null ? LocalDate.now(clock) : anchor);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<WorkSchedule> shifts = repository
                .findByUserIdAndWorkDateBetweenOrderByWorkDateAscShiftTypeAsc(
                        userId, weekStart, weekEnd);
        return new WeekScheduleResponse(
                weekStart,
                weekEnd,
                shifts.stream().map(ScheduleResponse::of).toList());
    }

    @Transactional(readOnly = true)
    public WeekCheckResponse check(LocalDate anchor) {
        UUID userId = userContext.requireUserId();
        LocalDate weekStart = mondayOf(anchor == null ? LocalDate.now(clock) : anchor);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<WorkSchedule> shifts = repository
                .findByUserIdAndWorkDateBetweenOrderByWorkDateAscShiftTypeAsc(
                        userId, weekStart, weekEnd);
        long morning = shifts.stream().filter(s -> s.getShiftType() == ShiftType.MORNING).count();
        long evening = shifts.stream().filter(s -> s.getShiftType() == ShiftType.EVENING).count();
        boolean complete = morning >= REQUIRED_MORNING && evening >= REQUIRED_EVENING;

        String message;
        if (complete) {
            message = String.format(
                    "Lịch tuần đã đủ: %d sáng, %d tối.", morning, evening);
        } else {
            message = String.format(
                    "Bạn chưa đăng ký đủ lịch tuần này. Cần tối thiểu %d sáng và %d tối. Hiện tại: %d sáng, %d tối.",
                    REQUIRED_MORNING, REQUIRED_EVENING, morning, evening);
        }

        return new WeekCheckResponse(
                weekStart, weekEnd,
                morning, evening,
                REQUIRED_MORNING, REQUIRED_EVENING,
                complete, message);
    }

    private static LocalDate mondayOf(LocalDate date) {
        int dow = date.getDayOfWeek().getValue();
        return date.minusDays(dow - DayOfWeek.MONDAY.getValue());
    }
}
