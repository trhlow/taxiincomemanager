import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:taxi_income/core/format.dart';
import 'package:taxi_income/core/theme.dart';
import 'package:taxi_income/features/orders/order_entry_screen.dart';

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

  testWidgets('order entry fits a small phone viewport without overflow',
      (tester) async {
    final previousOnError = FlutterError.onError;
    final overflowErrors = <FlutterErrorDetails>[];
    FlutterError.onError = (details) {
      final exception = details.exceptionAsString();
      if (exception.contains('A RenderFlex overflowed')) {
        overflowErrors.add(details);
      }
      previousOnError?.call(details);
    };

    addTearDown(() {
      FlutterError.onError = previousOnError;
    });

    await tester.binding.setSurfaceSize(const Size(320, 568));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    await tester.pumpWidget(
      ProviderScope(
        child: MaterialApp(
          theme: buildAppTheme(),
          home: const OrderEntryScreen(),
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(overflowErrors, isEmpty);
    expect(find.text('Nhập đơn'), findsOneWidget);

    await tester.drag(find.byType(ListView), const Offset(0, -500));
    await tester.pumpAndSettle();

    expect(overflowErrors, isEmpty);
    expect(find.text('Lưu đơn'), findsOneWidget);
  });
}
