import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api_client.dart';
import '../../core/providers.dart';
import '../../core/theme.dart';
import '../../widgets/info_banner.dart';
import '../../widgets/source_badge.dart';

class PersonalScreen extends ConsumerStatefulWidget {
  const PersonalScreen({super.key});

  @override
  ConsumerState<PersonalScreen> createState() => _PersonalScreenState();
}

class _PersonalScreenState extends ConsumerState<PersonalScreen> {
  late TextEditingController _baseUrlCtrl;
  bool _saving = false;
  bool _hasChanges = false;
  String _initialUrl = '';

  @override
  void initState() {
    super.initState();
    final storage = ref.read(localStorageProvider);
    _initialUrl = storage.baseUrl ?? ApiClient.defaultBaseUrl();
    _baseUrlCtrl = TextEditingController(text: _initialUrl);
    _baseUrlCtrl.addListener(_onChanged);
  }

  @override
  void dispose() {
    _baseUrlCtrl.dispose();
    super.dispose();
  }

  void _onChanged() {
    final changed = _baseUrlCtrl.text.trim() != _initialUrl.trim();
    if (changed != _hasChanges) {
      setState(() => _hasChanges = changed);
    }
  }

  Future<void> _save() async {
    setState(() => _saving = true);
    try {
      final storage = ref.read(localStorageProvider);
      await storage.setBaseUrl(_baseUrlCtrl.text.trim());
      _initialUrl = _baseUrlCtrl.text.trim();
      ref.invalidate(apiClientProvider);
      if (mounted) {
        setState(() {
          _saving = false;
          _hasChanges = false;
        });
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Đã lưu cấu hình API')),
        );
      }
    } catch (e) {
      if (mounted) {
        setState(() => _saving = false);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Lỗi: $e')),
        );
      }
    }
  }

  Future<void> _reset() async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(18)),
        title: const Text('Khởi tạo lại dữ liệu?'),
        content: const Text(
          'Hành động này sẽ xoá thông tin user khỏi máy này (không xoá dữ liệu trên server) và quay lại màn hình thiết lập.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('Huỷ'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            style: FilledButton.styleFrom(backgroundColor: Colors.redAccent),
            child: const Text('Khởi tạo lại'),
          ),
        ],
      ),
    );
    if (ok == true) {
      final storage = ref.read(localStorageProvider);
      await storage.clear();
      ref.read(currentUserProvider.notifier).state = null;
    }
  }

  @override
  Widget build(BuildContext context) {
    final user = ref.watch(currentUserProvider);
    return Scaffold(
      appBar: AppBar(
        backgroundColor: AppColors.bgScaffold,
        elevation: 0,
        scrolledUnderElevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new_rounded,
              color: AppColors.textPrimary, size: 18),
          onPressed: () => Navigator.of(context).maybePop(),
        ),
        title: const Text('Cá nhân',
            style: TextStyle(
              color: AppColors.textPrimary,
              fontWeight: FontWeight.w800,
              fontSize: 20,
            )),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
          children: [
            const Padding(
              padding: EdgeInsets.only(bottom: 14),
              child: Text(
                'Quản lý thông tin và cài đặt cá nhân',
                style: TextStyle(
                  fontSize: 13,
                  color: AppColors.textSecondary,
                ),
              ),
            ),
            _ProfileCard(name: user?.displayName ?? '...'),
            const SizedBox(height: 14),
            _SettingsCard(
              items: [
                _SettingItem(
                  icon: Icons.person_outline_rounded,
                  label: 'Tên hiển thị',
                  value: user?.displayName ?? '-',
                ),
                _SettingItem(
                  icon: Icons.badge_outlined,
                  label: 'User ID',
                  value: _shortenId(user?.userId),
                  isMono: true,
                ),
                _SettingItem(
                  icon: Icons.public_rounded,
                  label: 'API Base URL',
                  value: _baseUrlCtrl.text,
                  trailing: const Icon(Icons.edit_rounded,
                      size: 16, color: AppColors.primary),
                  onTap: _editApiUrl,
                ),
                const _SettingItem(
                  icon: Icons.schedule_rounded,
                  label: 'Múi giờ',
                  value: 'Asia/Ho_Chi_Minh',
                ),
                const _SettingItem(
                  icon: Icons.percent_rounded,
                  label: 'Fee mặc định',
                  value: '30%',
                ),
              ],
            ),
            const SizedBox(height: 14),
            const SectionHeader(title: 'Thông tin chu kỳ'),
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: AppColors.border),
              ),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    width: 40,
                    height: 40,
                    decoration: BoxDecoration(
                      color: AppColors.primary.withValues(alpha: 0.1),
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(Icons.calendar_month_rounded,
                        color: AppColors.primary, size: 20),
                  ),
                  const SizedBox(width: 12),
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        _CycleRow(label: 'Chu kỳ 1', range: '01 – 10'),
                        SizedBox(height: 6),
                        _CycleRow(label: 'Chu kỳ 2', range: '11 – 20'),
                        SizedBox(height: 6),
                        _CycleRow(label: 'Chu kỳ 3', range: '21 – cuối tháng'),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 18),
            const SectionHeader(title: 'Hành động'),
            FilledButton(
              onPressed: (_hasChanges && !_saving) ? _save : null,
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
                    const Icon(Icons.check_circle_outline_rounded,
                        color: Colors.white, size: 20),
                  const SizedBox(width: 8),
                  const Text('Lưu thay đổi'),
                ],
              ),
            ),
            const SizedBox(height: 10),
            OutlinedButton(
              onPressed: _reset,
              child: const Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.refresh_rounded,
                      size: 18, color: AppColors.primary),
                  SizedBox(width: 8),
                  Text('Khởi tạo lại dữ liệu'),
                ],
              ),
            ),
            const SizedBox(height: 14),
            const InfoBanner(
              message: 'Phiên bản: MVP 1.0',
              icon: Icons.info_outline_rounded,
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _editApiUrl() async {
    final tmp = TextEditingController(text: _baseUrlCtrl.text);
    final newUrl = await showModalBottomSheet<String>(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (ctx) => Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(ctx).viewInsets.bottom,
        ),
        child: Container(
          decoration: const BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
          ),
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  margin: const EdgeInsets.only(bottom: 16),
                  decoration: BoxDecoration(
                    color: AppColors.border,
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
              ),
              const Text(
                'Sửa API Base URL',
                style: TextStyle(
                  fontSize: 17,
                  fontWeight: FontWeight.w700,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 14),
              TextField(
                controller: tmp,
                keyboardType: TextInputType.url,
                decoration: const InputDecoration(
                  hintText: 'http://10.0.2.2:8081',
                  prefixIcon: Icon(Icons.link_rounded),
                ),
              ),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: () => Navigator.of(ctx).pop(tmp.text.trim()),
                child: const Text('Cập nhật'),
              ),
            ],
          ),
        ),
      ),
    );
    if (newUrl != null && newUrl.isNotEmpty) {
      _baseUrlCtrl.text = newUrl;
      _onChanged();
    }
  }

  String _shortenId(String? id) {
    if (id == null) return '-';
    if (id.length <= 12) return id;
    return '${id.substring(0, 8)}…${id.substring(id.length - 4)}';
  }
}

class _ProfileCard extends StatelessWidget {
  final String name;
  const _ProfileCard({required this.name});

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
        children: [
          Container(
            width: 56,
            height: 56,
            decoration: const BoxDecoration(
              shape: BoxShape.circle,
              gradient: LinearGradient(
                colors: [Color(0xFF60A5FA), AppColors.primary],
              ),
            ),
            child: const Icon(Icons.person_rounded,
                color: Colors.white, size: 32),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  style: const TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.w800,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 2),
                const Text(
                  'Ứng dụng quản lý thu nhập taxi cá nhân',
                  style: TextStyle(
                    fontSize: 12,
                    color: AppColors.textSecondary,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _SettingsCard extends StatelessWidget {
  final List<_SettingItem> items;
  const _SettingsCard({required this.items});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          for (var i = 0; i < items.length; i++) ...[
            items[i],
            if (i < items.length - 1)
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 14),
                child: Divider(height: 1),
              ),
          ],
        ],
      ),
    );
  }
}

class _SettingItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final Widget? trailing;
  final VoidCallback? onTap;
  final bool isMono;

  const _SettingItem({
    required this.icon,
    required this.label,
    required this.value,
    this.trailing,
    this.onTap,
    this.isMono = false,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(14),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
          child: Row(
            children: [
              Container(
                width: 36,
                height: 36,
                decoration: const BoxDecoration(
                  color: AppColors.surfaceMuted,
                  shape: BoxShape.circle,
                ),
                child: Icon(icon, color: AppColors.textSecondary, size: 18),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  label,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                    color: AppColors.textPrimary,
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Flexible(
                child: Text(
                  value,
                  textAlign: TextAlign.right,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: onTap != null
                        ? AppColors.primary
                        : AppColors.textSecondary,
                    fontFamily: isMono ? 'monospace' : null,
                  ),
                ),
              ),
              if (trailing != null) ...[
                const SizedBox(width: 6),
                trailing!,
              ] else if (onTap != null)
                const Padding(
                  padding: EdgeInsets.only(left: 4),
                  child: Icon(Icons.chevron_right_rounded,
                      size: 18, color: AppColors.textMuted),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _CycleRow extends StatelessWidget {
  final String label;
  final String range;
  const _CycleRow({required this.label, required this.range});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Text(
            label,
            style: const TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: AppColors.textPrimary,
            ),
          ),
        ),
        Text(
          range,
          style: const TextStyle(
            fontSize: 13,
            color: AppColors.textSecondary,
          ),
        ),
      ],
    );
  }
}
