class ScheduleItem {
  final String id;
  final DateTime workDate;
  final String shiftType;
  ScheduleItem({required this.id, required this.workDate, required this.shiftType});

  factory ScheduleItem.fromJson(Map<String, dynamic> json) => ScheduleItem(
        id: json['id'].toString(),
        workDate: DateTime.parse(json['workDate'] as String),
        shiftType: json['shiftType'] as String,
      );
}

class WeekSchedule {
  final DateTime weekStart;
  final DateTime weekEnd;
  final List<ScheduleItem> shifts;
  WeekSchedule({required this.weekStart, required this.weekEnd, required this.shifts});

  factory WeekSchedule.fromJson(Map<String, dynamic> json) => WeekSchedule(
        weekStart: DateTime.parse(json['weekStart'] as String),
        weekEnd: DateTime.parse(json['weekEnd'] as String),
        shifts: ((json['shifts'] as List?) ?? const [])
            .map((e) => ScheduleItem.fromJson(Map<String, dynamic>.from(e as Map)))
            .toList(),
      );

  bool has(DateTime day, String shift) {
    return shifts.any((s) =>
        s.workDate.year == day.year &&
        s.workDate.month == day.month &&
        s.workDate.day == day.day &&
        s.shiftType == shift);
  }
}

class WeekCheck {
  final DateTime weekStart;
  final DateTime weekEnd;
  final int morningCount;
  final int eveningCount;
  final int requiredMorning;
  final int requiredEvening;
  final bool isComplete;
  final String message;

  WeekCheck({
    required this.weekStart,
    required this.weekEnd,
    required this.morningCount,
    required this.eveningCount,
    required this.requiredMorning,
    required this.requiredEvening,
    required this.isComplete,
    required this.message,
  });

  factory WeekCheck.fromJson(Map<String, dynamic> json) => WeekCheck(
        weekStart: DateTime.parse(json['weekStart'] as String),
        weekEnd: DateTime.parse(json['weekEnd'] as String),
        morningCount: (json['morningCount'] as num).toInt(),
        eveningCount: (json['eveningCount'] as num).toInt(),
        requiredMorning: (json['requiredMorning'] as num).toInt(),
        requiredEvening: (json['requiredEvening'] as num).toInt(),
        isComplete: json['isComplete'] as bool,
        message: json['message'] as String,
      );
}
