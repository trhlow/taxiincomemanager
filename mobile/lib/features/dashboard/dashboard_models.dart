import 'package:flutter/foundation.dart';

import '../../core/json_parse.dart';

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
      today: parseLocalDate(json['today'].toString()),
      todayTotalNet: parseRequiredInt(json['todayTotalNet'], 'todayTotalNet'),
      todayOrderCount: parseRequiredInt(json['todayOrderCount'], 'todayOrderCount'),
      currentPeriod: PeriodSummary.fromJson(
          Map<String, dynamic>.from(json['currentPeriod'] as Map)),
      currentMonth: MonthSummary.fromJson(
          Map<String, dynamic>.from(json['currentMonth'] as Map)),
      totalTip: parseRequiredInt(json['totalTip'], 'totalTip'),
      totalFee: parseRequiredInt(json['totalFee'], 'totalFee'),
      workingDaysMonth: parseRequiredInt(json['workingDaysMonth'], 'workingDaysMonth'),
      workingDaysCurrentPeriod:
          parseRequiredInt(json['workingDaysCurrentPeriod'], 'workingDaysCurrentPeriod'),
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
        index: parseRequiredInt(json['index'], 'index'),
        start: parseLocalDate(json['start'].toString()),
        end: parseLocalDate(json['end'].toString()),
        totalNet: parseRequiredInt(json['totalNet'], 'totalNet'),
        orderCount: parseRequiredInt(json['orderCount'], 'orderCount'),
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
        year: parseRequiredInt(json['year'], 'year'),
        month: parseRequiredInt(json['month'], 'month'),
        totalNet: parseRequiredInt(json['totalNet'], 'totalNet'),
        orderCount: parseRequiredInt(json['orderCount'], 'orderCount'),
      );
}
