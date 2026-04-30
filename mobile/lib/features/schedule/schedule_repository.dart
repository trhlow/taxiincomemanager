import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/api_client.dart';
import '../../core/providers.dart';
import '../dashboard/dashboard_repository.dart';
import 'schedule_models.dart';

String _iso(DateTime d) =>
    '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

class ScheduleRepository {
  final ApiClient _api;
  ScheduleRepository(this._api);

  Future<WeekSchedule> week(DateTime weekStart) async {
    final res = await _api
        .getJson('/api/schedules/week', query: {'weekStart': _iso(weekStart)});
    return WeekSchedule.fromJson(res);
  }

  Future<WeekCheck> check(DateTime weekStart) async {
    final res = await _api.getJson('/api/schedules/week/check',
        query: {'weekStart': _iso(weekStart)});
    return WeekCheck.fromJson(res);
  }

  Future<void> add(DateTime workDate, String shiftType) async {
    await _api.postJson('/api/schedules', body: {
      'workDate': _iso(workDate),
      'shiftType': shiftType,
    });
  }

  Future<void> remove(DateTime workDate, String shiftType) async {
    await _api.delete('/api/schedules', query: {
      'workDate': _iso(workDate),
      'shiftType': shiftType,
    });
  }
}

final scheduleRepositoryProvider = Provider<ScheduleRepository>((ref) {
  return ScheduleRepository(ref.watch(apiClientProvider));
});

DateTime mondayOf(DateTime d) {
  final base = DateTime(d.year, d.month, d.day);
  final dow = base.weekday;
  return base.subtract(Duration(days: dow - DateTime.monday));
}

final selectedWeekStartProvider = StateProvider<DateTime>((ref) {
  return mondayOf(ref.watch(businessTodayProvider));
});

final weekScheduleProvider =
    FutureProvider.family<WeekSchedule, DateTime>((ref, weekStart) async {
  return ref.watch(scheduleRepositoryProvider).week(weekStart);
});

final weekCheckProvider =
    FutureProvider.family<WeekCheck, DateTime>((ref, weekStart) async {
  return ref.watch(scheduleRepositoryProvider).check(weekStart);
});
