import 'package:flutter_test/flutter_test.dart';
import 'package:taxi_income/core/json_parse.dart';

void main() {
  group('parseLocalDate', () {
    test('parses yyyy-MM-dd at local midnight', () {
      final d = parseLocalDate('2026-04-27');
      expect(d.year, 2026);
      expect(d.month, 4);
      expect(d.day, 27);
      expect(d.isUtc, isFalse);
    });

    test('uses date part before T for ISO timestamps', () {
      final d = parseLocalDate('2026-01-08T23:59:59.000Z');
      expect(d, DateTime(2026, 1, 8));
    });
  });
}
