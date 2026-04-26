import 'package:flutter/foundation.dart';

@immutable
class DashboardSummary {
  final DateTime today;
  final int todayTotalNet;
  final int todayOrderCount;
  final PeriodSummary currentPeriod;
  final MonthSummary currentMonth;
  final int totalTip;
  final int totalFee;
  final int workingDaysMonth;
  final int workingDaysCurrentPeriod;

  const DashboardSummary({
    required this.today,
    required this.todayTotalNet,
    required this.todayOrderCount,
    required this.currentPeriod,
    required this.currentMonth,
    required this.totalTip,
    required this.totalFee,
    required this.workingDaysMonth,
    required this.workingDaysCurrentPeriod,
  });

  factory DashboardSummary.fromJson(Map<String, dynamic> json) {
    return DashboardSummary(
      today: DateTime.parse(json['today'] as String),
      todayTotalNet: (json['todayTotalNet'] as num).toInt(),
      todayOrderCount: (json['todayOrderCount'] as num).toInt(),
      currentPeriod: PeriodSummary.fromJson(
          Map<String, dynamic>.from(json['currentPeriod'] as Map)),
      currentMonth: MonthSummary.fromJson(
          Map<String, dynamic>.from(json['currentMonth'] as Map)),
      totalTip: (json['totalTip'] as num).toInt(),
      totalFee: (json['totalFee'] as num).toInt(),
      workingDaysMonth: (json['workingDaysMonth'] as num).toInt(),
      workingDaysCurrentPeriod: (json['workingDaysCurrentPeriod'] as num).toInt(),
    );
  }
}

@immutable
class PeriodSummary {
  final int index;
  final DateTime start;
  final DateTime end;
  final int totalNet;
  final int orderCount;

  const PeriodSummary({
    required this.index,
    required this.start,
    required this.end,
    required this.totalNet,
    required this.orderCount,
  });

  factory PeriodSummary.fromJson(Map<String, dynamic> json) => PeriodSummary(
        index: (json['index'] as num).toInt(),
        start: DateTime.parse(json['start'] as String),
        end: DateTime.parse(json['end'] as String),
        totalNet: (json['totalNet'] as num).toInt(),
        orderCount: (json['orderCount'] as num).toInt(),
      );
}

@immutable
class MonthSummary {
  final int year;
  final int month;
  final int totalNet;
  final int orderCount;

  const MonthSummary({
    required this.year,
    required this.month,
    required this.totalNet,
    required this.orderCount,
  });

  factory MonthSummary.fromJson(Map<String, dynamic> json) => MonthSummary(
        year: (json['year'] as num).toInt(),
        month: (json['month'] as num).toInt(),
        totalNet: (json['totalNet'] as num).toInt(),
        orderCount: (json['orderCount'] as num).toInt(),
      );
}
