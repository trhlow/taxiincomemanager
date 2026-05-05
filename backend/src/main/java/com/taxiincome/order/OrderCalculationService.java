package com.taxiincome.order;

import com.taxiincome.common.ApiException;
import com.taxiincome.common.MoneyUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Pure validation and money math for taxi orders (same rules as mobile OrderMoneyCalc defaults).
 */
@Service
public class OrderCalculationService {

    public static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.300");
    public static final long MAX_ORDER_AMOUNT = 100_000_000L;
    public static final long MAX_TIP_AMOUNT = 20_000_000L;

    /** Pure calculation — unit-tested via {@link com.taxiincome.order.OrderCalculationServiceTest}. */
    public Calculation calculate(long orderAmount, long tipAmount,
                                 short taxiCount, BigDecimal feeRate) {
        if (orderAmount < 0) {
            throw ApiException.badRequest("INVALID_AMOUNT", "Tiền đơn không được âm");
        }
        if (orderAmount == 0) {
            throw ApiException.badRequest("INVALID_AMOUNT", "Tiền đơn phải lớn hơn 0");
        }
        if (orderAmount > MAX_ORDER_AMOUNT) {
            throw ApiException.badRequest("INVALID_AMOUNT", "Tiền đơn quá lớn");
        }
        if (tipAmount < 0) {
            throw ApiException.badRequest("INVALID_AMOUNT", "Tiền bo không được âm");
        }
        if (tipAmount > MAX_TIP_AMOUNT) {
            throw ApiException.badRequest("INVALID_AMOUNT", "Tiền bo quá lớn");
        }
        if (taxiCount != 1 && taxiCount != 2) {
            throw ApiException.badRequest("INVALID_TAXI_COUNT", "Số tài chỉ được 1 hoặc 2");
        }
        if (feeRate == null || feeRate.signum() < 0 || feeRate.compareTo(BigDecimal.ONE) > 0) {
            throw ApiException.badRequest("INVALID_FEE_RATE", "Tỷ lệ cước phải trong [0, 1]");
        }
        long feeAmount = MoneyUtils.multiplyRate(orderAmount, feeRate);
        long subtotal = orderAmount - feeAmount + tipAmount;
        long netAmount = (taxiCount == 2) ? MoneyUtils.halfRoundUp(subtotal) : subtotal;
        return new Calculation(feeAmount, subtotal, netAmount);
    }

    public record Calculation(long feeAmount, long subtotal, long netAmount) {
    }
}
