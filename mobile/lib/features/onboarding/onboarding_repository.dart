import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/api_client.dart';
import '../../core/providers.dart';

class OnboardingRepository {
  final ApiClient _api;
  OnboardingRepository(this._api);

  /// Returns the saved (id, displayName) pair.
  Future<({String userId, String displayName})> initUser(String displayName) async {
    final res = await _api.postJson('/api/users/init', body: {
      'displayName': displayName.trim(),
    });
    final id = res['id'].toString();
    final name = (res['displayName'] ?? displayName).toString();
    await _api.storage.setUser(userId: id, displayName: name);
    return (userId: id, displayName: name);
  }
}

final onboardingRepositoryProvider = Provider<OnboardingRepository>((ref) {
  return OnboardingRepository(ref.watch(apiClientProvider));
});
