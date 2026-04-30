import 'dart:async';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:taxi_income/core/api_client.dart';
import 'package:taxi_income/core/local_storage.dart';

void main() {
  test('wraps malformed successful responses as INVALID_RESPONSE', () async {
    SharedPreferences.setMockInitialValues({});
    final storage = LocalStorage(
      await SharedPreferences.getInstance(),
      const FlutterSecureStorage(),
    );
    final dio = Dio(BaseOptions(baseUrl: 'http://localhost'));
    dio.httpClientAdapter = _StaticAdapter('[]');
    final api = ApiClient.forTest(dio, storage);

    await expectLater(
      api.getJson('/broken'),
      throwsA(
        isA<ApiException>()
            .having((e) => e.code, 'code', 'INVALID_RESPONSE')
            .having((e) => e.message, 'message', contains('server')),
      ),
    );
  });
}

class _StaticAdapter implements HttpClientAdapter {
  final String body;

  _StaticAdapter(this.body);

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    return ResponseBody.fromString(
      body,
      200,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}
