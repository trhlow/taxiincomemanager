import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';

class _ThousandsFormatter extends TextInputFormatter {
  static final _fmt = NumberFormat.decimalPattern('vi_VN');

  @override
  TextEditingValue formatEditUpdate(
      TextEditingValue oldValue, TextEditingValue newValue) {
    final digits = newValue.text.replaceAll(RegExp(r'[^0-9]'), '');
    if (digits.isEmpty) {
      return const TextEditingValue(text: '');
    }
    final n = int.parse(digits);
    final formatted = _fmt.format(n);
    return TextEditingValue(
      text: formatted,
      selection: TextSelection.collapsed(offset: formatted.length),
    );
  }
}

class MoneyInput extends StatelessWidget {
  final TextEditingController controller;
  final String label;
  final String? hintText;
  final IconData? icon;
  final ValueChanged<int>? onChanged;

  const MoneyInput({
    super.key,
    required this.controller,
    required this.label,
    this.hintText,
    this.icon,
    this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final hasLabel = label.isNotEmpty;
    return TextField(
      controller: controller,
      keyboardType: TextInputType.number,
      inputFormatters: [_ThousandsFormatter()],
      decoration: InputDecoration(
        labelText: hasLabel ? label : null,
        hintText: hintText ?? '0',
        prefixIcon: icon != null ? Icon(icon) : null,
        suffixText: 'đ',
      ),
      onChanged: (v) {
        if (onChanged == null) return;
        final digits = v.replaceAll(RegExp(r'[^0-9]'), '');
        onChanged!(digits.isEmpty ? 0 : int.parse(digits));
      },
    );
  }
}
