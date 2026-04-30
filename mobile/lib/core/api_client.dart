import 'dart:io' show Platform;
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'local_storage.dart';

class ApiException implements Exception {
  final int? statusCode;
  final String code;
  final String message;
  ApiException({this.statusCode, required this.code, required this.message});

  @override
  String toString() => '[$code] $message';
}

class ApiClient {
  final Dio _dio;
  final LocalStorage _storage;

  ApiClient._(this._dio, this._storage);

  @visibleForTesting
  factory ApiClient.forTest(Dio dio, LocalStorage storage) {
    return ApiClient._(dio, storage);
  }

  static String defaultBaseUrl() {
    if (kIsWeb) return 'http://localhost:8081';
    try {
      if (Platform.isAndroid) return 'http://10.0.2.2:8081';
    } catch (_) {}
    return 'http://localhost:8081';
  }

  /// Resolves API key from build-time defines (no runtime env on mobile).
  ///
  /// - Pass `--dart-define=TAXI_API_KEY=...` (matches server `APP_API_KEY`).
  /// - For local dev without a key file, use `--dart-define=DEV_MODE=true` to
  ///   allow the shared dev key, or rely on **debug** builds (see below).
  /// - **Release** always requires `TAXI_API_KEY` (never implicit dev key).
  /// - **Profile** requires `TAXI_API_KEY` or `DEV_MODE=true`.
  /// - **Debug** (e.g. `flutter test`, `flutter run`): falls back to dev key so
  ///   CI and local workflows work without repeating defines.
  static String defaultApiKey() {
    const fromEnv = String.fromEnvironment('TAXI_API_KEY');
    const devMode = bool.fromEnvironment('DEV_MODE', defaultValue: false);
    if (fromEnv.isNotEmpty) return fromEnv;
    if (kReleaseMode) {
      throw StateError(
        'Missing TAXI_API_KEY. Release builds must pass '
        '--dart-define=TAXI_API_KEY=<server key>.',
      );
    }
    if (devMode) return 'dev-local-api-key';
    if (kProfileMode) {
      throw StateError(
        'Missing TAXI_API_KEY. Profile builds must pass '
        '--dart-define=TAXI_API_KEY=... or --dart-define=DEV_MODE=true for local dev.',
      );
    }
    return 'dev-local-api-key';
  }

  static ApiClient create(LocalStorage storage) {
    final apiKey = defaultApiKey();
    final base = storage.baseUrl ?? defaultBaseUrl();
    final dio = Dio(BaseOptions(
      baseUrl: base,
      connectTimeout: const Duration(seconds: 8),
      sendTimeout: const Duration(seconds: 8),
      receiveTimeout: const Duration(seconds: 15),
      contentType: 'application/json',
      responseType: ResponseType.json,
    ));
    dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) {
        options.headers['X-Api-Key'] = apiKey;
        final token = storage.accessToken;
        if (token != null && token.isNotEmpty) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
    ));
    return ApiClient._(dio, storage);
  }

  Dio get dio => _dio;
  LocalStorage get storage => _storage;

  String get baseUrl => _dio.options.baseUrl;
  set baseUrl(String url) => _dio.options.baseUrl = url;

  Future<T> _wrap<T>(Future<Response<dynamic>> Function() call,
      T Function(dynamic data) parse) async {
    try {
      final res = await call();
      return parse(res.data);
    } on DioException catch (e) {
      final status = e.response?.statusCode;
      final body = e.response?.data;
      if (body is Map<String, dynamic>) {
        throw ApiException(
          statusCode: status,
          code: (body['code'] ?? 'UNKNOWN').toString(),
          message: (body['message'] ?? 'Có lỗi xảy ra').toString(),
        );
      }
      throw ApiException(
        statusCode: status,
        code: 'NETWORK_ERROR',
        message: e.message ?? 'Không kết nối được tới máy chủ',
      );
    } on FormatException {
      throw ApiException(
        code: 'INVALID_RESPONSE',
        message: 'Dữ liệu từ server không hợp lệ. Vui lòng thử lại.',
      );
    } on TypeError {
      throw ApiException(
        code: 'INVALID_RESPONSE',
        message: 'Dữ liệu từ server không đúng định dạng. Vui lòng thử lại.',
      );
    }
  }

  Future<Map<String, dynamic>> getJson(String path,
      {Map<String, dynamic>? query}) {
    return _wrap(
      () => _dio.get(path, queryParameters: query),
      (data) => Map<String, dynamic>.from(data as Map),
    );
  }

  Future<List<dynamic>> getList(String path, {Map<String, dynamic>? query}) {
    return _wrap(
      () => _dio.get(path, queryParameters: query),
      (data) => List<dynamic>.from(data as List),
    );
  }

  Future<Map<String, dynamic>> postJson(String path, {Object? body}) {
    return _wrap(
      () => _dio.post(path, data: body),
      (data) => Map<String, dynamic>.from(data as Map),
    );
  }

  Future<void> delete(String path, {Map<String, dynamic>? query}) async {
    try {
      await _dio.delete(path, queryParameters: query);
    } on DioException catch (e) {
      final body = e.response?.data;
      if (body is Map<String, dynamic>) {
        throw ApiException(
          statusCode: e.response?.statusCode,
          code: (body['code'] ?? 'UNKNOWN').toString(),
          message: (body['message'] ?? 'Có lỗi xảy ra').toString(),
        );
      }
      throw ApiException(
        statusCode: e.response?.statusCode,
        code: 'NETWORK_ERROR',
        message: e.message ?? 'Không kết nối được tới máy chủ',
      );
    }
  }
}
