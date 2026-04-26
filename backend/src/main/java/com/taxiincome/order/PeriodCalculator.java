package com.taxiincome.order;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Chu kỳ 10 ngày trong tháng:
 * - Kỳ 1: ngày 01 → 10
 * - Kỳ 2: ngày 11 → 20
 * - Kỳ 3: ngày 21 → cuối tháng
 */
@Component
public class PeriodCalculator {

    public record Period(LocalDate start, LocalDate endInclusive, int index) {
    }

    public Period periodOf(LocalDate date) {
        int day = date.getDayOfMonth();
        int year = date.getYear();
        int month = date.getMonthValue();
        if (day <= 10) {
            return new Period(LocalDate.of(year, month, 1),
                    LocalDate.of(year, month, 10), 1);
        }
        if (day <= 20) {
            return new Period(LocalDate.of(year, month, 11),
                    LocalDate.of(year, month, 20), 2);
        }
        LocalDate firstOfMonth = LocalDate.of(year, month, 1);
        return new Period(LocalDate.of(year, month, 21),
                firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth()), 3);
    }

    public Period currentPeriod(LocalDate today) {
        return periodOf(today);
    }
}
