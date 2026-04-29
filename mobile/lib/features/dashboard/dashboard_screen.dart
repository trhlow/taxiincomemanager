import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api_client.dart';
import '../../core/format.dart';
import '../../core/network_feedback.dart';
import '../../core/providers.dart';
import '../../core/theme.dart';
import '../../widgets/hero_gradient_card.dart';
import '../../widgets/stat_card.dart';
import '../../widgets/source_badge.dart';
import '../orders/order_models.dart';
import '../orders/order_repository.dart';
import 'dashboard_models.dart';
import 'dashboard_repository.dart';

class DashboardScreen extends ConsumerWidget {
  final VoidCallback? onOpenPersonal;
  const DashboardScreen({super.key, this.onOpenPersonal});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(currentUserProvider);
    final summaryAsync = ref.watch(dashboardSummaryProvider);

    return Scaffold(
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () async {
            ref.invalidate(dashboardSummaryProvider);
            final s =
                await ref.read(dashboardSummaryProvider.future);
            ref.invalidate(dailyOrdersProvider(s.today));
          },
          child: ListView(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
            children: [
              summaryAsync.when(
                loading: () => const Padding(
                  padding: EdgeInsets.symmetric(vertical: 80),
                  child: Center(child: CircularProgressIndicator()),
                ),
                error: (e, _) => _buildError(context, ref, e),
                data: (s) {
                  final todayOrdersAsync =
                      ref.watch(dailyOrdersProvider(s.today));
                  return Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      _Header(today: s.today, onBellTap: () {}),
                      const SizedBox(height: 14),
                      _GreetingCard(
                        name: user?.displayName ?? '...',
                        onTap: onOpenPersonal,
                      ),
                      const SizedBox(height: 14),
                      _content(context, s, todayOrdersAsync),
                    ],
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _content(
    BuildContext context,
    DashboardSummary s,
    AsyncValue<DailyOrders> todayOrdersAsync,
  ) {
    final period = s.currentPeriod;
    final month = s.currentMonth;
    final lastOrder = todayOrdersAsync.maybeWhen(
      data: (d) => d.orders.isNotEmpty ? d.orders.last : null,
      orElse: () => null,
    );

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        HeroGradientCard(
          title: 'Chu kỳ hiện tại',
          label: '${formatDate(period.start)} - ${formatDate(period.end)}',
          badge: 'Kỳ ${period.index}',
          value: formatVnd(period.totalNet),
          icon: Icons.event_available_rounded,
          trailing: Padding(
            padding: const EdgeInsets.only(top: 6),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  'Thực nhận trong chu kỳ',
                  style: TextStyle(
                    color: Colors.white.withValues(alpha: 0.8),
                    fontSize: 12,
                  ),
                ),
                const SizedBox(width: 4),
                Icon(Icons.info_outline,
                    color: Colors.white.withValues(alpha: 0.6), size: 14),
              ],
            ),
          ),
        ),
        const SizedBox(height: 14),
        GridView.count(
          crossAxisCount: 2,
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          childAspectRatio: 1.55,
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          children: [
            StatCard(
              label: 'Hôm nay',
              value: formatVnd(s.todayTotalNet),
              icon: Icons.account_balance_wallet_rounded,
              color: AppColors.accentBlue,
            ),
            StatCard(
              label: 'Tháng này',
              value: formatVnd(month.totalNet),
              icon: Icons.trending_up_rounded,
              color: AppColors.accentGreen,
            ),
            StatCard(
              label: 'Tổng bo',
              value: formatVnd(s.totalTip),
              icon: Icons.card_giftcard_rounded,
              color: AppColors.accentPurple,
            ),
            StatCard(
              label: 'Tổng cước',
              value: formatVnd(s.totalFee),
              icon: Icons.receipt_long_rounded,
              color: AppColors.accentOrange,
            ),
            StatCard(
              label: 'Ngày làm / tháng',
              value: '${s.workingDaysMonth} ngày',
              icon: Icons.calendar_today_rounded,
              color: AppColors.accentBlue,
            ),
            StatCard(
              label: 'Ngày làm / chu kỳ',
              value: '${s.workingDaysCurrentPeriod} ngày',
              icon: Icons.event_note_rounded,
              color: AppColors.accentTeal,
            ),
          ],
        ),
        const SizedBox(height: 14),
        if (lastOrder != null) _LastOrderCard(order: lastOrder),
      ],
    );
  }

  Widget _buildError(BuildContext context, WidgetRef ref, Object e) {
    final msg =
        e is ApiException ? userFacingApiMessage(e) : e.toString();
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 40),
      child: Column(
        children: [
          const Icon(Icons.error_outline,
              size: 48, color: AppColors.accentOrange),
          const SizedBox(height: 12),
          Text(msg, textAlign: TextAlign.center),
          const SizedBox(height: 16),
          FilledButton.tonal(
            onPressed: () => ref.invalidate(dashboardSummaryProvider),
            child: const Text('Thử lại'),
          ),
        ],
      ),
    );
  }
}

class _Header extends StatelessWidget {
  final DateTime today;
  final VoidCallback onBellTap;
  const _Header({required this.today, required this.onBellTap});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Tổng quan',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textPrimary,
                  letterSpacing: -0.5,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                'Hôm nay, ${formatDate(today)}',
                style: const TextStyle(
                  fontSize: 13,
                  color: AppColors.textSecondary,
                ),
              ),
            ],
          ),
        ),
        Container(
          decoration: BoxDecoration(
            color: Colors.white,
            shape: BoxShape.circle,
            border: Border.all(color: AppColors.border),
          ),
          child: IconButton(
            icon: const Icon(Icons.notifications_none_rounded,
                color: AppColors.textPrimary),
            onPressed: onBellTap,
          ),
        ),
      ],
    );
  }
}

class _GreetingCard extends StatelessWidget {
  final String name;
  final VoidCallback? onTap;
  const _GreetingCard({required this.name, this.onTap});

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(18),
        child: Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(18),
            border: Border.all(color: AppColors.border),
          ),
          child: Row(
            children: [
              Container(
                width: 52,
                height: 52,
                decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                    colors: [Color(0xFF60A5FA), Color(0xFF2563EB)],
                  ),
                ),
                child: const Icon(Icons.person_rounded,
                    color: Colors.white, size: 30),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Chào $name',
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w700,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 2),
                    const Text(
                      'Theo dõi thu nhập taxi mỗi ngày',
                      style: TextStyle(
                        fontSize: 12,
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
              const _MiniTaxiArt(),
            ],
          ),
        ),
      ),
    );
  }
}

class _MiniTaxiArt extends StatelessWidget {
  const _MiniTaxiArt();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 60,
      height: 44,
      decoration: BoxDecoration(
        color: AppColors.surfaceMuted,
        borderRadius: BorderRadius.circular(12),
      ),
      child: const Icon(Icons.local_taxi_rounded,
          color: AppColors.primary, size: 26),
    );
  }
}

class _LastOrderCard extends StatelessWidget {
  final OrderModel order;
  const _LastOrderCard({required this.order});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Container(
            width: 56,
            height: 56,
            decoration: BoxDecoration(
              color: AppColors.surfaceMuted,
              borderRadius: BorderRadius.circular(14),
            ),
            child: const Icon(Icons.local_taxi_rounded,
                color: AppColors.primary, size: 30),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    const Icon(Icons.access_time_rounded,
                        size: 16, color: AppColors.textSecondary),
                    const SizedBox(width: 6),
                    const Expanded(
                      child: Text(
                        'Đơn gần nhất:',
                        style: TextStyle(
                          fontSize: 13,
                          color: AppColors.textSecondary,
                        ),
                      ),
                    ),
                    Text(
                      formatVnd(order.orderAmount),
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: AppColors.primary,
                      ),
                    ),
                  ],
                ),
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 6),
                  child: Divider(height: 1),
                ),
                Row(
                  children: [
                    const Icon(Icons.account_balance_wallet_outlined,
                        size: 16, color: AppColors.accentGreen),
                    const SizedBox(width: 6),
                    const Expanded(
                      child: Text(
                        'Thực nhận:',
                        style: TextStyle(
                          fontSize: 13,
                          color: AppColors.textSecondary,
                        ),
                      ),
                    ),
                    Text(
                      formatVnd(order.netAmount),
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: AppColors.accentGreen,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                Row(
                  children: [
                    Text(
                      order.orderTime,
                      style: const TextStyle(
                        fontSize: 11,
                        color: AppColors.textMuted,
                      ),
                    ),
                    const SizedBox(width: 8),
                    SourceBadge(source: order.sourceType),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
