import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:taxi_income/features/dashboard/dashboard_models.dart';
import 'package:taxi_income/features/dashboard/dashboard_repository.dart';
import 'package:taxi_income/features/orders/order_repository.dart';
import 'package:taxi_income/features/schedule/schedule_repository.dart';

void main() {
  test('uses server today for shared business date providers', () async {
    final serverToday = DateTime(2026, 4, 29);
    final container = ProviderContainer(
      overrides: [
        dashboardSummaryProvider.overrideWith((ref) async {
          return DashboardSummary(
            today: serverToday,
            todayTotalNet: 0,
            todayOrderCount: 0,
            currentPeriod: PeriodSummary(
              index: 3,
              start: DateTime(2026, 4, 21),
              end: DateTime(2026, 4, 30),
              totalNet: 0,
              orderCount: 0,
            ),
            currentMonth: const MonthSummary(
              year: 2026,
              month: 4,
              totalNet: 0,
              orderCount: 0,
            ),
            totalTip: 0,
            totalFee: 0,
            workingDaysMonth: 0,
            workingDaysCurrentPeriod: 0,
          );
        }),
      ],
    );
    addTearDown(container.dispose);

    await container.read(dashboardSummaryProvider.future);

    expect(container.read(businessTodayProvider), serverToday);
    expect(container.read(selectedHistoryDateProvider), serverToday);
    expect(container.read(selectedWeekStartProvider), DateTime(2026, 4, 27));
  });
}
