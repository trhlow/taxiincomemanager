// Defensive parsing for API JSON to avoid runtime crashes on unexpected types.

int parseRequiredInt(Object? v, String field) {
  if (v == null) {
    throw FormatException('Thiếu $field');
  }
  if (v is int) return v;
  if (v is num) return v.toInt();
  if (v is String) {
    final p = int.tryParse(v.trim());
    if (p != null) return p;
  }
  throw FormatException('$field không hợp lệ');
}

double parseRequiredDouble(Object? v, String field) {
  if (v == null) {
    throw FormatException('Thiếu $field');
  }
  if (v is double) return v;
  if (v is num) return v.toDouble();
  if (v is String) {
    final p = double.tryParse(v.trim());
    if (p != null) return p;
  }
  throw FormatException('$field không hợp lệ');
}

String? parseOptionalString(Object? v) {
  if (v == null) return null;
  final s = v.toString();
  if (s.isEmpty) return null;
  return s;
}

/// Parses a calendar date from `YYYY-MM-DD` or an ISO-8601 string (uses the date part only).
/// Returns local [DateTime] at midnight — avoids TZ shift from `DateTime.parse("...Z")`.
DateTime parseLocalDate(String raw) {
  final p = raw.trim();
  if (p.isEmpty) throw const FormatException('Ngày rỗng');
  final sep = p.indexOf('T');
  final dateOnly = sep == -1 ? p : p.substring(0, sep);
  final parts = dateOnly.split('-');
  if (parts.length != 3) throw FormatException('Ngày không hợp lệ: $raw');
  final y = int.tryParse(parts[0]);
  final m = int.tryParse(parts[1]);
  final d = int.tryParse(parts[2]);
  if (y == null || m == null || d == null) {
    throw FormatException('Ngày không hợp lệ: $raw');
  }
  return DateTime(y, m, d);
}

/// Backend sends `LocalTime` as `HH:mm:ss` — keep `HH:mm` for UI labels.
String normalizeOrderTime(Object? raw) {
  final s = raw?.toString() ?? '';
  if (s.length >= 5) return s.substring(0, 5);
  return s;
}
