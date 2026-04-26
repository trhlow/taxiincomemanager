import 'package:flutter_test/flutter_test.dart';
import 'package:taxi_income/core/format.dart';

void main() {
  test('formatVnd formats integer with thousand separators and dong suffix', () {
    expect(formatVnd(160000), '160.000 đ');
    expect(formatVnd(0), '0 đ');
    expect(formatVnd(1234567), '1.234.567 đ');
  });

  test('parseVndInput strips non-digit chars', () {
    expect(parseVndInput('200.000'), 200000);
    expect(parseVndInput(''), 0);
    expect(parseVndInput('abc'), 0);
  });
}
