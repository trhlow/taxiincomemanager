package com.taxiincome.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {

    Optional<WorkSchedule> findByUserIdAndWorkDateAndShiftType(
            UUID userId, LocalDate workDate, ShiftType shiftType);

    List<WorkSchedule> findByUserIdAndWorkDateBetweenOrderByWorkDateAscShiftTypeAsc(
            UUID userId, LocalDate start, LocalDate endInclusive);

    @Modifying
    @Query("DELETE FROM WorkSchedule s WHERE s.userId = :userId AND s.workDate = :date AND s.shiftType = :type")
    int deleteByUserAndDateAndShift(@Param("userId") UUID userId,
                                    @Param("date") LocalDate date,
                                    @Param("type") ShiftType type);
}
