import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api_client.dart';
import '../../core/format.dart';
import '../../core/network_feedback.dart';
import '../../core/theme.dart';
import '../../widgets/info_banner.dart';
import '../../widgets/source_badge.dart';
import 'schedule_models.dart';
import 'schedule_repository.dart';

class ScheduleScreen extends ConsumerWidget {
  const ScheduleScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final weekStart = ref.watch(selectedWeekStartProvider);
    final scheduleAsync = ref.watch(weekScheduleProvider(weekStart));
    final checkAsync = ref.watch(weekCheckProvider(weekStart));
    final weekEnd = weekStart.add(const Duration(days: 6));

    return Scaffold(
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
          children: [
            ScreenHeader(
              title: 'Lịch tuần',
              subtitle: 'Tuần ${formatDate(weekStart)} - ${formatDate(weekEnd)}',
              actions: [
                Container(
                  decoration: BoxDecoration(
                    color: Colors.white,
                    shape: BoxShape.circle,
                    border: Border.all(color: AppColors.border),
                  ),
                  child: IconButton(
                    icon: const Icon(Icons.notifications_none_rounded,
                        color: AppColors.textPrimary),
                    onPressed: () {},
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                _NavBtn(
                  icon: Icons.chevron_left_rounded,
                  onTap: () {
                    ref.read(selectedWeekStartProvider.notifier).state =
                        weekStart.subtract(const Duration(days: 7));
                  },
                ),
                const Spacer(),
                _NavBtn(
                  icon: Icons.chevron_right_rounded,
                  onTap: () {
                    ref.read(selectedWeekStartProvider.notifier).state =
                        weekStart.add(const Duration(days: 7));
                  },
                ),
              ],
            ),
            const SizedBox(height: 12),
            checkAsync.when(
              data: (c) => _CheckCard(check: c),
              loading: () => const SizedBox(height: 80),
              error: (e, _) => Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: Material(
                  color: AppColors.accentOrange.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(12),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 12, vertical: 10),
                    child: Row(
                      children: [
                        const Icon(Icons.warning_amber_rounded,
                            color: AppColors.accentOrange, size: 20),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            'Không tải được kiểm tra lịch: $e',
                            style: const TextStyle(
                              fontSize: 12,
                              color: AppColors.textPrimary,
                              height: 1.35,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 14),
            scheduleAsync.when(
              data: (week) => _DayList(weekStart: weekStart, week: week),
              loading: () => const Padding(
                padding: EdgeInsets.symmetric(vertical: 60),
                child: Center(child: CircularProgressIndicator()),
              ),
              error: (e, _) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 40),
                child: Center(child: Text(e.toString())),
              ),
            ),
            const SizedBox(height: 14),
            checkAsync.maybeWhen(
              data: (c) => InfoBanner(
                message:
                    'Yêu cầu tuần: ${c.requiredMorning} ca sáng, ${c.requiredEvening} ca tối',
              ),
              orElse: () => const SizedBox.shrink(),
            ),
          ],
        ),
      ),
    );
  }
}

class _NavBtn extends StatelessWidget {
  final IconData icon;
  final VoidCallback onTap;
  const _NavBtn({required this.icon, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(14),
        child: Container(
          width: 44,
          height: 44,
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(14),
            border: Border.all(color: AppColors.border),
          ),
          child: Icon(icon, color: AppColors.textPrimary, size: 22),
        ),
      ),
    );
  }
}

class _CheckCard extends StatelessWidget {
  final WeekCheck check;
  const _CheckCard({required this.check});

  @override
  Widget build(BuildContext context) {
    final ok = check.isComplete;
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: AppColors.accentGreen.withValues(alpha: 0.12),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.event_available_rounded,
                    color: AppColors.accentGreen, size: 22),
              ),
              const SizedBox(width: 12),
              const Text(
                'Kiểm tra lịch',
                style: TextStyle(
                  fontSize: 17,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textPrimary,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _StatusPill(
                label: 'Sáng: ${check.morningCount}/${check.requiredMorning}',
                color: check.morningCount >= check.requiredMorning
                    ? AppColors.accentGreen
                    : AppColors.accentOrange,
              ),
              _StatusPill(
                label: 'Tối: ${check.eveningCount}/${check.requiredEvening}',
                color: check.eveningCount >= check.requiredEvening
                    ? AppColors.accentGreen
                    : AppColors.accentOrange,
              ),
              _StatusPill(
                label: ok ? 'Đủ lịch' : 'Thiếu lịch',
                icon: ok
                    ? Icons.check_circle_rounded
                    : Icons.warning_amber_rounded,
                color: ok ? AppColors.accentGreen : AppColors.accentOrange,
                filled: true,
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            check.message,
            style: const TextStyle(
              fontSize: 13,
              color: AppColors.textSecondary,
              height: 1.4,
            ),
          ),
        ],
      ),
    );
  }
}

class _StatusPill extends StatelessWidget {
  final String label;
  final Color color;
  final IconData? icon;
  final bool filled;
  const _StatusPill({
    required this.label,
    required this.color,
    this.icon,
    this.filled = false,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: filled ? color.withValues(alpha: 0.14) : Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withValues(alpha: 0.4)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icon != null) ...[
            Icon(icon, size: 14, color: color),
            const SizedBox(width: 4),
          ],
          Text(
            label,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: color,
            ),
          ),
        ],
      ),
    );
  }
}

class _DayList extends ConsumerWidget {
  final DateTime weekStart;
  final WeekSchedule week;
  const _DayList({required this.weekStart, required this.week});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      children: List.generate(7, (i) {
        final day = DateTime(weekStart.year, weekStart.month, weekStart.day)
            .add(Duration(days: i));
        return _DayRow(day: day, dowLabel: weekdayShortVi[i], week: week);
      }),
    );
  }
}

class _DayRow extends ConsumerWidget {
  final DateTime day;
  final String dowLabel;
  final WeekSchedule week;
  const _DayRow({required this.day, required this.dowLabel, required this.week});

  Future<void> _toggle(BuildContext context, WidgetRef ref, String shift) async {
    final repo = ref.read(scheduleRepositoryProvider);
    final has = week.has(day, shift);
    try {
      if (has) {
        await repo.remove(day, shift);
      } else {
        await repo.add(day, shift);
      }
      ref.invalidate(weekScheduleProvider);
      ref.invalidate(weekCheckProvider);
    } on ApiException catch (e) {
      if (context.mounted) {
        showApiErrorSnack(context, e);
      }
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final today = DateTime.now();
    final isToday = day.year == today.year &&
        day.month == today.month &&
        day.day == today.day;
    final hasMorning = week.has(day, 'MORNING');
    final hasEvening = week.has(day, 'EVENING');

    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: isToday
                ? AppColors.primary.withValues(alpha: 0.4)
                : AppColors.border,
          ),
        ),
        child: Row(
          children: [
            SizedBox(
              width: 60,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    dowLabel,
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                      color: isToday
                          ? AppColors.primary
                          : AppColors.textPrimary,
                    ),
                  ),
                  Text(
                    '${day.day.toString().padLeft(2, '0')}/${day.month.toString().padLeft(2, '0')}',
                    style: const TextStyle(
                      fontSize: 11,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ],
              ),
            ),
            Expanded(
              child: Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  _ShiftToggle(
                    label: 'Sáng',
                    icon: Icons.wb_sunny_outlined,
                    selected: hasMorning,
                    onTap: () => _toggle(context, ref, 'MORNING'),
                  ),
                  const SizedBox(width: 10),
                  _ShiftToggle(
                    label: 'Tối',
                    icon: Icons.nightlight_round,
                    selected: hasEvening,
                    onTap: () => _toggle(context, ref, 'EVENING'),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ShiftToggle extends StatelessWidget {
  final String label;
  final IconData icon;
  final bool selected;
  final VoidCallback onTap;
  const _ShiftToggle({
    required this.label,
    required this.icon,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(20),
        child: Container(
          width: 92,
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
          decoration: BoxDecoration(
            color: selected
                ? AppColors.primary.withValues(alpha: 0.1)
                : Colors.white,
            borderRadius: BorderRadius.circular(22),
            border: Border.all(
              color: selected ? AppColors.primary : AppColors.border,
              width: selected ? 1.5 : 1,
            ),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              if (selected)
                Container(
                  width: 18,
                  height: 18,
                  decoration: const BoxDecoration(
                    color: AppColors.primary,
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.check, color: Colors.white, size: 12),
                )
              else
                Icon(icon, size: 16, color: AppColors.textSecondary),
              const SizedBox(width: 6),
              Text(
                label,
                style: TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                  color: selected ? AppColors.primary : AppColors.textPrimary,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
