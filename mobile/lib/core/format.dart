import 'package:intl/intl.dart';

final NumberFormat _vnd = NumberFormat.decimalPattern('vi_VN');
final DateFormat _date = DateFormat('dd/MM/yyyy');
final DateFormat _dateLong = DateFormat('EEEE dd/MM/yyyy', 'vi_VN');
final DateFormat _time = DateFormat('HH:mm');

String formatVnd(num? amount) {
  if (amount == null) return '0 đ';
  return '${_vnd.format(amount)} đ';
}

String formatVndPlain(num? amount) {
  if (amount == null) return '0';
  return _vnd.format(amount);
}

int parseVndInput(String raw) {
  if (raw.isEmpty) return 0;
  final digits = raw.replaceAll(RegExp(r'[^0-9]'), '');
  if (digits.isEmpty) return 0;
  return int.parse(digits);
}

String formatDate(DateTime d) => _date.format(d);
String formatDateLong(DateTime d) => _dateLong.format(d);
String formatTime(DateTime d) => _time.format(d);

String shiftLabel(String shiftType) {
  switch (shiftType) {
    case 'MORNING':
      return 'Sáng';
    case 'EVENING':
      return 'Tối';
    default:
      return shiftType;
  }
}

const List<String> weekdayShortVi = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];
