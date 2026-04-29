/// Pure Dart mirror of backend [MoneyUtils] + default 30% fee path used by [OrderCalculationService].
///
/// Backend: `feeAmount = roundHalfUp(amount * rate)` with rate as [BigDecimal],
/// default `0.300`. For rates with at most 3 decimal places, `rate * 1000` is exact.
class OrderMoneyCalc {
  OrderMoneyCalc._();

  /// Default fee 30% — matches `OrderCalculationService.DEFAULT_FEE_RATE` ("0.300").
  static const int defaultFeeRateMillis = 300;

  /// Matches `MoneyUtils.multiplyRate` for nonnegative amount and rate in [0, 1]
  /// expressed as millis-per-thousand (300 => 0.300).
  static int multiplyRate(int amount, int rateMillis) {
    if (amount <= 0 || rateMillis <= 0) return 0;
    if (rateMillis >= 1000) return amount;
    return (amount * rateMillis + 500) ~/ 1000;
  }

  /// Matches `MoneyUtils.halfRoundUp` when dividing by 2 (nonnegative subtotal).
  static int halfRoundUpDivideByTwo(int subtotal) {
    if (subtotal <= 0) return 0;
    return (subtotal + 1) ~/ 2;
  }

  static int subtotal(int orderAmount, int feeAmount, int tipAmount) =>
      orderAmount - feeAmount + tipAmount;

  static int netAmount(int subtotal, int taxiCount) {
    if (taxiCount == 2) return halfRoundUpDivideByTwo(subtotal);
    return subtotal;
  }
}
