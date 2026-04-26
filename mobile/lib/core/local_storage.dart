import 'package:shared_preferences/shared_preferences.dart';

class LocalStorage {
  static const _kUserId = 'userId';
  static const _kDisplayName = 'displayName';
  static const _kBaseUrl = 'apiBaseUrl';

  final SharedPreferences _prefs;

  LocalStorage(this._prefs);

  static Future<LocalStorage> create() async {
    final prefs = await SharedPreferences.getInstance();
    return LocalStorage(prefs);
  }

  String? get userId => _prefs.getString(_kUserId);
  String? get displayName => _prefs.getString(_kDisplayName);
  String? get baseUrl => _prefs.getString(_kBaseUrl);

  Future<void> setUser({required String userId, required String displayName}) async {
    await _prefs.setString(_kUserId, userId);
    await _prefs.setString(_kDisplayName, displayName);
  }

  Future<void> setBaseUrl(String url) async {
    await _prefs.setString(_kBaseUrl, url);
  }

  Future<void> clear() async {
    await _prefs.remove(_kUserId);
    await _prefs.remove(_kDisplayName);
  }
}
