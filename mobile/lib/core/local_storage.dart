import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Local preferences for non-sensitive keys; access token is stored in secure storage.
class LocalStorage {
  static const _kUserId = 'userId';
  static const _kDisplayName = 'displayName';
  static const _kLegacyPrefsTokenKey = 'accessToken';
  static const _kSecureAccessTokenKey = 'taxi_secure_access_token';

  final SharedPreferences _prefs;
  final FlutterSecureStorage _secureStorage;

  String? _cachedAccessToken;

  LocalStorage(this._prefs, this._secureStorage);

  static Future<LocalStorage> create() async {
    final prefs = await SharedPreferences.getInstance();
    const secureStorage = FlutterSecureStorage(
      aOptions: AndroidOptions(encryptedSharedPreferences: true),
    );
    final storage = LocalStorage(prefs, secureStorage);
    await storage._migrateTokenFromPrefsIfNeeded();
    return storage;
  }

  /// One-time copy from SharedPreferences (legacy) then read into memory for sync [accessToken] getters.
  Future<void> _migrateTokenFromPrefsIfNeeded() async {
    var secured = await _secureStorage.read(key: _kSecureAccessTokenKey);
    final legacy = _prefs.getString(_kLegacyPrefsTokenKey);
    if ((secured == null || secured.isEmpty) &&
        legacy != null &&
        legacy.isNotEmpty) {
      await _secureStorage.write(key: _kSecureAccessTokenKey, value: legacy);
      await _prefs.remove(_kLegacyPrefsTokenKey);
      secured = legacy;
    }
    _cachedAccessToken =
        (secured != null && secured.isNotEmpty) ? secured : null;
  }

  String? get userId => _prefs.getString(_kUserId);
  String? get displayName => _prefs.getString(_kDisplayName);
  String? get baseUrl => _prefs.getString('apiBaseUrl');

  /// Cached secure token (set at startup and on [setSession]/[clear]).
  String? get accessToken => _cachedAccessToken;

  Future<void> setSession({
    required String userId,
    required String displayName,
    required String accessToken,
  }) async {
    await _prefs.setString(_kUserId, userId);
    await _prefs.setString(_kDisplayName, displayName);
    await _secureStorage.write(
      key: _kSecureAccessTokenKey,
      value: accessToken,
    );
    _cachedAccessToken = accessToken;
  }

  Future<void> setBaseUrl(String url) async {
    await _prefs.setString('apiBaseUrl', url);
  }

  Future<void> clear() async {
    await _prefs.remove(_kUserId);
    await _prefs.remove(_kDisplayName);
    await _secureStorage.delete(key: _kSecureAccessTokenKey);
    _cachedAccessToken = null;
  }
}
