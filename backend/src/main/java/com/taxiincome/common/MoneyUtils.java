package com.taxiincome.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    private MoneyUtils() {
    }

    /** Round half-up to a whole VND amount. */
    public static long roundVnd(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public static long multiplyRate(long amount, BigDecimal rate) {
        return roundVnd(BigDecimal.valueOf(amount).multiply(rate));
    }

    public static long halfRoundUp(long amount) {
        return roundVnd(BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(2L), 2, RoundingMode.HALF_UP));
    }
}
