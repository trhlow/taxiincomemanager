import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/api_client.dart';
import '../../core/providers.dart';
import 'dashboard_models.dart';

class DashboardRepository {
  final ApiClient _api;
  DashboardRepository(this._api);

  Future<DashboardSummary> summary() async {
    final res = await _api.getJson('/api/dashboard');
    return DashboardSummary.fromJson(res);
  }
}

final dashboardRepositoryProvider = Provider<DashboardRepository>((ref) {
  return DashboardRepository(ref.watch(apiClientProvider));
});

final dashboardSummaryProvider = FutureProvider<DashboardSummary>((ref) async {
  final repo = ref.watch(dashboardRepositoryProvider);
  return repo.summary();
});
