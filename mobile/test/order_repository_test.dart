import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:taxi_income/core/api_client.dart';
import 'package:taxi_income/core/local_storage.dart';
import 'package:taxi_income/features/orders/order_repository.dart';

void main() {
  test('create uses caller-provided idempotency key for retry-safe submit',
      () async {
    SharedPreferences.setMockInitialValues({});
    final storage = LocalStorage(
      await SharedPreferences.getInstance(),
      const FlutterSecureStorage(),
    );
    final dio = Dio(BaseOptions(baseUrl: 'http://localhost'));
    final adapter = _OrderCreateAdapter();
    dio.httpClientAdapter = adapter;
    final repo = OrderRepository(ApiClient.forTest(dio, storage));

    final saved = await repo.create(
      orderAmount: 100000,
      tipAmount: 0,
      taxiCount: 1,
      idempotencyKey: 'retry-key-1',
    );

    expect(saved.id, 'order-1');
    expect(adapter.lastIdempotencyKey, 'retry-key-1');
  });
}

class _OrderCreateAdapter implements HttpClientAdapter {
  String? lastIdempotencyKey;

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    lastIdempotencyKey = options.headers['Idempotency-Key']?.toString();
    return ResponseBody.fromString(
      jsonEncode({
        'id': 'order-1',
        'orderAmount': 100000,
        'feeRate': 0.3,
        'feeAmount': 30000,
        'tipAmount': 0,
        'taxiCount': 1,
        'subtotal': 70000,
        'netAmount': 70000,
        'orderDate': '2026-05-16',
        'orderTime': '08:30:00',
        'sourceType': 'MANUAL',
      }),
      201,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}
