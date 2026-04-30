import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/api_client.dart';
import '../../core/providers.dart';
import '../dashboard/dashboard_repository.dart';
import 'order_models.dart';

class OrderRepository {
  final ApiClient _api;
  OrderRepository(this._api);

  Future<OrderModel> create({
    required int orderAmount,
    required int tipAmount,
    required int taxiCount,
    String? note,
  }) async {
    final res = await _api.postJson('/api/orders', body: {
      'orderAmount': orderAmount,
      'tipAmount': tipAmount,
      'taxiCount': taxiCount,
      if (note != null && note.trim().isNotEmpty) 'note': note.trim(),
    });
    return OrderModel.fromJson(res);
  }

  Future<DailyOrders> byDate(DateTime date) async {
    final iso =
        '${date.year.toString().padLeft(4, '0')}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
    final res = await _api.getJson('/api/orders/by-date', query: {'date': iso});
    return DailyOrders.fromJson(res);
  }
}

final orderRepositoryProvider = Provider<OrderRepository>((ref) {
  return OrderRepository(ref.watch(apiClientProvider));
});

final dailyOrdersProvider =
    FutureProvider.family<DailyOrders, DateTime>((ref, date) async {
  final repo = ref.watch(orderRepositoryProvider);
  return repo.byDate(DateTime(date.year, date.month, date.day));
});

final selectedHistoryDateProvider = StateProvider<DateTime>((ref) {
  return ref.watch(businessTodayProvider);
});
