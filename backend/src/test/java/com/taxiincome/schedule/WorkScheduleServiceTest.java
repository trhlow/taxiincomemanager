package com.taxiincome.schedule;

import com.taxiincome.common.UserContext;
import com.taxiincome.schedule.dto.CreateScheduleRequest;
import com.taxiincome.schedule.dto.ScheduleResponse;
import com.taxiincome.schedule.dto.ScheduleUpsertResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkScheduleServiceTest {

    @Mock
    WorkScheduleRepository repository;

    @Mock
    UserContext userContext;

    private final Clock clock =
            Clock.fixed(Instant.parse("2026-05-04T10:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private WorkScheduleService service;

    @BeforeEach
    void setUp() {
        service = new WorkScheduleService(repository, userContext, clock);
    }

    @Test
    void upsert_whenRowAlreadyExists_returnsExisting() {
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 10);
        WorkSchedule row = new WorkSchedule();
        row.setId(UUID.randomUUID());
        row.setUserId(userId);
        row.setWorkDate(date);
        row.setShiftType(ShiftType.MORNING);

        when(userContext.requireUserId()).thenReturn(userId);
        when(repository.findByUserIdAndWorkDateAndShiftType(userId, date, ShiftType.MORNING))
                .thenReturn(Optional.of(row));

        ScheduleUpsertResult out = service.upsert(new CreateScheduleRequest(date, ShiftType.MORNING));

        assertThat(out.created()).isFalse();
        assertThat(out.schedule()).isEqualTo(ScheduleResponse.of(row));
        verify(repository).findByUserIdAndWorkDateAndShiftType(userId, date, ShiftType.MORNING);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void upsert_whenConcurrentInsert_winsRetryReturnsExisting() {
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 11);
        WorkSchedule winner = new WorkSchedule();
        winner.setId(UUID.randomUUID());
        winner.setUserId(userId);
        winner.setWorkDate(date);
        winner.setShiftType(ShiftType.EVENING);

        when(userContext.requireUserId()).thenReturn(userId);
        when(repository.findByUserIdAndWorkDateAndShiftType(userId, date, ShiftType.EVENING))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winner));
        when(repository.saveAndFlush(any(WorkSchedule.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        ScheduleUpsertResult out = service.upsert(new CreateScheduleRequest(date, ShiftType.EVENING));

        assertThat(out.created()).isFalse();
        assertThat(out.schedule()).isEqualTo(ScheduleResponse.of(winner));
    }

    @Test
    void upsert_whenIntegrityFailsAndStillMissing_rethrows() {
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 12);
        DataIntegrityViolationException cause =
                new DataIntegrityViolationException("other constraint");

        when(userContext.requireUserId()).thenReturn(userId);
        when(repository.findByUserIdAndWorkDateAndShiftType(userId, date, ShiftType.MORNING))
                .thenReturn(Optional.empty(), Optional.empty());
        when(repository.saveAndFlush(any(WorkSchedule.class))).thenThrow(cause);

        assertThatThrownBy(() -> service.upsert(new CreateScheduleRequest(date, ShiftType.MORNING)))
                .isSameAs(cause);
    }

    @Test
    void upsert_whenNewRow_returnsCreated() {
        UUID userId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 13);
        WorkSchedule saved = new WorkSchedule();
        saved.setId(UUID.randomUUID());
        saved.setUserId(userId);
        saved.setWorkDate(date);
        saved.setShiftType(ShiftType.MORNING);

        when(userContext.requireUserId()).thenReturn(userId);
        when(repository.findByUserIdAndWorkDateAndShiftType(userId, date, ShiftType.MORNING))
                .thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(WorkSchedule.class))).thenReturn(saved);

        ScheduleUpsertResult out = service.upsert(new CreateScheduleRequest(date, ShiftType.MORNING));

        assertThat(out.created()).isTrue();
        assertThat(out.schedule()).isEqualTo(ScheduleResponse.of(saved));
    }
}
