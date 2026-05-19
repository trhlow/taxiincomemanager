import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:taxi_income/core/api_client.dart';
import 'package:taxi_income/core/format.dart';
import 'package:taxi_income/core/local_storage.dart';
import 'package:taxi_income/core/theme.dart';
import 'package:taxi_income/features/orders/order_entry_screen.dart';
import 'package:taxi_income/features/orders/order_repository.dart';

void main() {
  test('formatVnd formats integer with thousand separators and dong suffix',
      () {
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

  testWidgets('order entry reuses pending idempotency key after failed submit',
      (tester) async {
    SharedPreferences.setMockInitialValues({});
    final storage = LocalStorage(
      await SharedPreferences.getInstance(),
      const FlutterSecureStorage(),
    );
    final dio = Dio(BaseOptions(baseUrl: 'http://localhost'));
    final adapter = _FailingOrderCreateAdapter();
    dio.httpClientAdapter = adapter;
    final repo = OrderRepository(ApiClient.forTest(dio, storage));

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          orderRepositoryProvider.overrideWithValue(repo),
        ],
        child: MaterialApp(
          theme: buildAppTheme(),
          home: const OrderEntryScreen(),
        ),
      ),
    );

    await tester.enterText(find.byType(TextField).first, '100000');
    await tester.scrollUntilVisible(
      find.text('Lưu đơn'),
      200,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.tap(find.text('Lưu đơn'));
    await tester.pumpAndSettle();
    await tester.pump(const Duration(seconds: 5));
    await tester.pumpAndSettle();

    await tester.scrollUntilVisible(
      find.text('Lưu đơn'),
      200,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.tap(find.text('Lưu đơn'));
    await tester.pumpAndSettle();

    expect(adapter.idempotencyKeys, hasLength(2));
    expect(adapter.idempotencyKeys.first, isNotEmpty);
    expect(adapter.idempotencyKeys[1], adapter.idempotencyKeys.first);
  });
}

class _FailingOrderCreateAdapter implements HttpClientAdapter {
  final List<String> idempotencyKeys = [];

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    idempotencyKeys.add(options.headers['Idempotency-Key']?.toString() ?? '');
    return ResponseBody.fromString(
      jsonEncode({
        'code': 'SERVER_ERROR',
        'message': 'Lưu đơn thất bại',
      }),
      500,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}
