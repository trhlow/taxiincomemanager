package com.taxiincome.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases lấy thẳng từ file HoangLongGap.xlsx (sheet NhapDon) để chốt công thức.
 */
class OrderServiceCalculationTest {

    private static final BigDecimal RATE = new BigDecimal("0.30");

    @Test
    void case1_amount444k_tip0_taxi2_halfNet() {
        OrderService.Calculation c = OrderService.calculate(444_000L, 0L, (short) 2, RATE);
        assertThat(c.feeAmount()).isEqualTo(133_200L);
        assertThat(c.subtotal()).isEqualTo(310_800L);
        assertThat(c.netAmount()).isEqualTo(155_400L);
    }

    @Test
    void case2_amount821k_tip20k_taxi1_fullNet() {
        OrderService.Calculation c = OrderService.calculate(821_000L, 20_000L, (short) 1, RATE);
        assertThat(c.feeAmount()).isEqualTo(246_300L);
        assertThat(c.subtotal()).isEqualTo(594_700L);
        assertThat(c.netAmount()).isEqualTo(594_700L);
    }

    @Test
    void case3_amount25k_tip130k_taxi1_fullNet() {
        OrderService.Calculation c = OrderService.calculate(25_000L, 130_000L, (short) 1, RATE);
        assertThat(c.feeAmount()).isEqualTo(7_500L);
        assertThat(c.subtotal()).isEqualTo(147_500L);
        assertThat(c.netAmount()).isEqualTo(147_500L);
    }

    @Test
    void case_classicSpec_amount200k_tip20k_taxi1() {
        OrderService.Calculation c = OrderService.calculate(200_000L, 20_000L, (short) 1, RATE);
        assertThat(c.feeAmount()).isEqualTo(60_000L);
        assertThat(c.subtotal()).isEqualTo(160_000L);
        assertThat(c.netAmount()).isEqualTo(160_000L);
    }
}
