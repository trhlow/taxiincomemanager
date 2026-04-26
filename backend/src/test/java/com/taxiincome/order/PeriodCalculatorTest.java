package com.taxiincome.order;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodCalculatorTest {

    private final PeriodCalculator calc = new PeriodCalculator();

    @Test
    void firstDayOfMonth_isPeriod1() {
        PeriodCalculator.Period p = calc.periodOf(LocalDate.of(2026, 4, 1));
        assertThat(p.index()).isEqualTo(1);
        assertThat(p.start()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(p.endInclusive()).isEqualTo(LocalDate.of(2026, 4, 10));
    }

    @Test
    void day10_isPeriod1() {
        PeriodCalculator.Period p = calc.periodOf(LocalDate.of(2026, 4, 10));
        assertThat(p.index()).isEqualTo(1);
    }

    @Test
    void day11_isPeriod2() {
        PeriodCalculator.Period p = calc.periodOf(LocalDate.of(2026, 4, 11));
        assertThat(p.index()).isEqualTo(2);
        assertThat(p.start()).isEqualTo(LocalDate.of(2026, 4, 11));
        assertThat(p.endInclusive()).isEqualTo(LocalDate.of(2026, 4, 20));
    }

    @Test
    void day20_isPeriod2() {
        PeriodCalculator.Period p = calc.periodOf(LocalDate.of(2026, 4, 20));
        assertThat(p.index()).isEqualTo(2);
    }

    @Test
    void day21_isPeriod3() {
        PeriodCalculator.Period p = calc.periodOf(LocalDate.of(2026, 4, 21));
        assertThat(p.index()).isEqualTo(3);
        assertThat(p.start()).isEqualTo(LocalDate.of(2026, 4, 21));
        assertThat(p.endInclusive()).isEqualTo(LocalDate.of(2026, 4, 30));
    }

    @Test
    void period3_endsOnLastDay_for31DayMonth() {
        PeriodCalculator.Period p = calc.periodOf(LocalDate.of(2026, 1, 25));
        assertThat(p.index()).isEqualTo(3);
        assertThat(p.start()).isEqualTo(LocalDate.of(2026, 1, 21));
        assertThat(p.endInclusive()).isEqualTo(LocalDate.of(2026, 1, 31));
    }

    @Test
    void period3_endsOnLastDay_forFebruary_nonLeap() {
        PeriodCalculator.Period p = calc.periodOf(LocalDate.of(2026, 2, 25));
        assertThat(p.endInclusive()).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    void period3_endsOnLastDay_forFebruary_leap() {
        PeriodCalculator.Period p = calc.periodOf(LocalDate.of(2024, 2, 25));
        assertThat(p.endInclusive()).isEqualTo(LocalDate.of(2024, 2, 29));
    }
}
