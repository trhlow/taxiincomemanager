import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api_client.dart';
import '../../core/format.dart';
import '../../core/theme.dart';
import '../../widgets/info_banner.dart';
import '../../widgets/money_input.dart';
import '../../widgets/source_badge.dart';
import '../dashboard/dashboard_repository.dart';
import 'order_repository.dart';

class OrderEntryScreen extends ConsumerStatefulWidget {
  const OrderEntryScreen({super.key});

  @override
  ConsumerState<OrderEntryScreen> createState() => _OrderEntryScreenState();
}

class _OrderEntryScreenState extends ConsumerState<OrderEntryScreen> {
  final _amountCtrl = TextEditingController();
  final _tipCtrl = TextEditingController();
  final _noteCtrl = TextEditingController();
  int _amount = 0;
  int _tip = 0;
  int _taxiCount = 1;
  bool _saving = false;

  static const double _feeRate = 0.30;

  @override
  void dispose() {
    _amountCtrl.dispose();
    _tipCtrl.dispose();
    _noteCtrl.dispose();
    super.dispose();
  }

  int get _fee => (_amount * _feeRate).round();
  int get _subtotal => _amount - _fee + _tip;
  int get _net => _taxiCount == 2 ? ((_subtotal + 1) ~/ 2) : _subtotal;

  Future<void> _save() async {
    if (_amount <= 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vui lòng nhập tiền đơn')),
      );
      return;
    }
    setState(() => _saving = true);
    try {
      final repo = ref.read(orderRepositoryProvider);
      final saved = await repo.create(
        orderAmount: _amount,
        tipAmount: _tip,
        taxiCount: _taxiCount,
        note: _noteCtrl.text,
      );
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Row(
            children: [
              const Icon(Icons.check_circle_outline_rounded,
                  color: Colors.white, size: 18),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  'Đã lưu. Cước ${formatVnd(saved.feeAmount)} — Thực nhận ${formatVnd(saved.netAmount)}',
                ),
              ),
            ],
          ),
        ),
      );
      _amountCtrl.clear();
      _tipCtrl.clear();
      _noteCtrl.clear();
      setState(() {
        _amount = 0;
        _tip = 0;
        _taxiCount = 1;
      });
      ref.invalidate(dashboardSummaryProvider);
      ref.invalidate(dailyOrdersProvider);
    } on ApiException catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.message)),
      );
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
          children: [
            const ScreenHeader(title: 'Nhập đơn', subtitle: 'Tạo đơn mới'),
            const SizedBox(height: 14),
            const InfoBanner(
              message: 'Thực nhận = Tiền đơn − Cước + Bo',
              trailing: InfoChip(
                label: 'Cước mặc định 30%',
                color: AppColors.primary,
              ),
            ),
            const SizedBox(height: 14),
            _Card(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const _FieldLabel('Số tiền đơn'),
                  const SizedBox(height: 6),
                  MoneyInput(
                    controller: _amountCtrl,
                    label: '',
                    onChanged: (v) => setState(() => _amount = v),
                  ),
                  const SizedBox(height: 14),
                  const _FieldLabel('Tiền bo'),
                  const SizedBox(height: 6),
                  MoneyInput(
                    controller: _tipCtrl,
                    label: '',
                    onChanged: (v) => setState(() => _tip = v),
                  ),
                  const SizedBox(height: 14),
                  const _FieldLabel('Ghi chú'),
                  const SizedBox(height: 6),
                  TextField(
                    controller: _noteCtrl,
                    decoration: const InputDecoration(
                      hintText: 'VD: Cuốc sân bay',
                    ),
                  ),
                  const SizedBox(height: 14),
                  const _FieldLabel('Số tài (tài xế cùng đi)'),
                  const SizedBox(height: 8),
                  _TaxiCountSegmented(
                    value: _taxiCount,
                    onChanged: (v) => setState(() => _taxiCount = v),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 14),
            _Card(
              child: Column(
                children: [
                  _PreviewRow(
                    icon: Icons.percent_rounded,
                    color: AppColors.accentBlue,
                    label: 'Cước (30%)',
                    value: formatVnd(_fee),
                  ),
                  const SizedBox(height: 10),
                  _PreviewRow(
                    icon: Icons.card_giftcard_rounded,
                    color: AppColors.accentPurple,
                    label: 'Tiền bo',
                    value: formatVnd(_tip),
                  ),
                  const Padding(
                    padding: EdgeInsets.symmetric(vertical: 12),
                    child: Divider(height: 1),
                  ),
                  _PreviewRow(
                    icon: Icons.account_balance_wallet_rounded,
                    color: AppColors.accentGreen,
                    label: 'Thực nhận',
                    value: formatVnd(_net),
                    big: true,
                  ),
                  if (_taxiCount == 2)
                    const Padding(
                      padding: EdgeInsets.only(top: 6),
                      child: Align(
                        alignment: Alignment.centerRight,
                        child: Text(
                          '(chia đôi với tài xế khác)',
                          style: TextStyle(
                            fontSize: 12,
                            color: AppColors.textSecondary,
                          ),
                        ),
                      ),
                    ),
                ],
              ),
            ),
            const SizedBox(height: 18),
            FilledButton(
              onPressed: _saving ? null : _save,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  if (_saving)
                    const SizedBox(
                      width: 18,
                      height: 18,
                      child: CircularProgressIndicator(
                          strokeWidth: 2.5, color: Colors.white),
                    )
                  else
                    const Icon(Icons.save_rounded,
                        color: Colors.white, size: 20),
                  const SizedBox(width: 8),
                  const Text('Lưu đơn'),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _Card extends StatelessWidget {
  final Widget child;
  const _Card({required this.child});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: child,
    );
  }
}

class _FieldLabel extends StatelessWidget {
  final String text;
  const _FieldLabel(this.text);

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: const TextStyle(
        fontSize: 13,
        color: AppColors.textSecondary,
        fontWeight: FontWeight.w500,
      ),
    );
  }
}

class _TaxiCountSegmented extends StatelessWidget {
  final int value;
  final ValueChanged<int> onChanged;
  const _TaxiCountSegmented({required this.value, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: _SegmentChip(
            label: '1 tài',
            icon: Icons.person_rounded,
            active: value == 1,
            onTap: () => onChanged(1),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _SegmentChip(
            label: '2 tài (chia đôi)',
            icon: Icons.people_alt_rounded,
            active: value == 2,
            onTap: () => onChanged(2),
          ),
        ),
      ],
    );
  }
}

class _SegmentChip extends StatelessWidget {
  final String label;
  final IconData icon;
  final bool active;
  final VoidCallback onTap;
  const _SegmentChip({
    required this.label,
    required this.icon,
    required this.active,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 10),
          decoration: BoxDecoration(
            color: active
                ? AppColors.primary.withValues(alpha: 0.08)
                : Colors.white,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: active ? AppColors.primary : AppColors.border,
              width: active ? 1.5 : 1,
            ),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon,
                  size: 18,
                  color: active ? AppColors.primary : AppColors.textSecondary),
              const SizedBox(width: 6),
              Flexible(
                child: Text(
                  label,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: active ? AppColors.primary : AppColors.textPrimary,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _PreviewRow extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String label;
  final String value;
  final bool big;
  const _PreviewRow({
    required this.icon,
    required this.color,
    required this.label,
    required this.value,
    this.big = false,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 32,
          height: 32,
          decoration: BoxDecoration(
            color: color.withValues(alpha: 0.12),
            shape: BoxShape.circle,
          ),
          child: Icon(icon, color: color, size: 18),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: Text(
            label,
            style: TextStyle(
              fontSize: big ? 16 : 14,
              fontWeight: big ? FontWeight.w700 : FontWeight.w500,
              color: AppColors.textPrimary,
            ),
          ),
        ),
        Text(
          value,
          style: TextStyle(
            fontSize: big ? 22 : 15,
            fontWeight: FontWeight.w800,
            color: color,
            letterSpacing: -0.3,
          ),
        ),
      ],
    );
  }
}
