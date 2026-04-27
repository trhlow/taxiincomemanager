package com.taxiincome.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void splitHalf_roundsOddSubtotalUp() {
        OrderService.Calculation c = OrderService.calculate(101L, 0L, (short) 2, BigDecimal.ZERO);
        assertThat(c.netAmount()).isEqualTo(51L);
    }

    @Test
    void rejectsInvalidTaxiCount() {
        assertThatThrownBy(() -> OrderService.calculate(100_000L, 0L, (short) 3, RATE))
                .hasMessage("Số tài chỉ được 1 hoặc 2");
    }

    @Test
    void rejectsInvalidFeeRate() {
        assertThatThrownBy(() -> OrderService.calculate(100_000L, 0L, (short) 1, new BigDecimal("1.1")))
                .hasMessage("Tỷ lệ cước phải trong [0, 1]");
    }

    @Test
    void rejectsUnrealisticMoneyValues() {
        assertThatThrownBy(() -> OrderService.calculate(100_000_001L, 0L, (short) 1, RATE))
                .hasMessage("Tiền đơn quá lớn");
        assertThatThrownBy(() -> OrderService.calculate(100_000L, 20_000_001L, (short) 1, RATE))
                .hasMessage("Tiền bo quá lớn");
    }
}
