import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'api_client.dart';
import 'local_storage.dart';

final localStorageProvider = Provider<LocalStorage>((ref) {
  throw UnimplementedError('LocalStorage must be overridden in main()');
});

final apiClientProvider = Provider<ApiClient>((ref) {
  final storage = ref.watch(localStorageProvider);
  return ApiClient.create(storage);
});

class CurrentUser {
  final String userId;
  final String displayName;
  const CurrentUser({required this.userId, required this.displayName});
}

final currentUserProvider = StateProvider<CurrentUser?>((ref) {
  final storage = ref.watch(localStorageProvider);
  final id = storage.userId;
  final name = storage.displayName;
  if (id == null || name == null) return null;
  return CurrentUser(userId: id, displayName: name);
});
