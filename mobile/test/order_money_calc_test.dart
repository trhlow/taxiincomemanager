import 'package:flutter_test/flutter_test.dart';
import 'package:taxi_income/core/order_money_calc.dart';

void main() {
  group('OrderMoneyCalc.multiplyRate', () {
    test('matches backend HALF_UP for 30% (rate millis = 300)', () {
      expect(OrderMoneyCalc.multiplyRate(0, 300), 0);
      expect(OrderMoneyCalc.multiplyRate(1, 300), 0);
      expect(OrderMoneyCalc.multiplyRate(2, 300), 1);
      expect(OrderMoneyCalc.multiplyRate(10, 300), 3);
      expect(OrderMoneyCalc.multiplyRate(100000, 300), 30000);
    });
  });

  group('OrderMoneyCalc.netAmount (2 tài)', () {
    test('half-up divide by 2 for nonnegative int', () {
      expect(OrderMoneyCalc.netAmount(5, 2), 3);
      expect(OrderMoneyCalc.netAmount(4, 2), 2);
      expect(OrderMoneyCalc.netAmount(1, 2), 1);
      expect(OrderMoneyCalc.netAmount(0, 2), 0);
    });
  });

  group('preview chain', () {
    test('sample order matches manual formula', () {
      const amount = 160000;
      const tip = 10000;
      final fee = OrderMoneyCalc.multiplyRate(amount, 300);
      expect(fee, 48000);
      final sub = OrderMoneyCalc.subtotal(amount, fee, tip);
      expect(sub, 122000);
      expect(OrderMoneyCalc.netAmount(sub, 1), 122000);
      expect(OrderMoneyCalc.netAmount(sub, 2), 61000);
    });
  });
}
