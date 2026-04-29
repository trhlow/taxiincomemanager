import '../../core/json_parse.dart';

class OrderModel {
  final String id;
  final int orderAmount;
  final double feeRate;
  final int feeAmount;
  final int tipAmount;
  final int taxiCount;
  final int subtotal;
  final int netAmount;
  final DateTime orderDate;
  final String orderTime;
  final String? note;
  final String sourceType;

  OrderModel({
    required this.id,
    required this.orderAmount,
    required this.feeRate,
    required this.feeAmount,
    required this.tipAmount,
    required this.taxiCount,
    required this.subtotal,
    required this.netAmount,
    required this.orderDate,
    required this.orderTime,
    this.note,
    this.sourceType = 'MANUAL',
  });

  factory OrderModel.fromJson(Map<String, dynamic> json) {
    return OrderModel(
      id: json['id'].toString(),
      orderAmount: parseRequiredInt(json['orderAmount'], 'orderAmount'),
      feeRate: parseRequiredDouble(json['feeRate'], 'feeRate'),
      feeAmount: parseRequiredInt(json['feeAmount'], 'feeAmount'),
      tipAmount: parseRequiredInt(json['tipAmount'], 'tipAmount'),
      taxiCount: parseRequiredInt(json['taxiCount'], 'taxiCount'),
      subtotal: parseRequiredInt(json['subtotal'], 'subtotal'),
      netAmount: parseRequiredInt(json['netAmount'], 'netAmount'),
      orderDate: parseLocalDate(json['orderDate'].toString()),
      orderTime: normalizeOrderTime(json['orderTime']),
      note: parseOptionalString(json['note']),
      sourceType: parseOptionalString(json['sourceType']) ?? 'MANUAL',
    );
  }
}

class DailyOrders {
  final DateTime date;
  final int orderCount;
  final int totalOrderAmount;
  final int totalFee;
  final int totalTip;
  final int totalNet;
  final List<OrderModel> orders;

  DailyOrders({
    required this.date,
    required this.orderCount,
    required this.totalOrderAmount,
    required this.totalFee,
    required this.totalTip,
    required this.totalNet,
    required this.orders,
  });

  factory DailyOrders.fromJson(Map<String, dynamic> json) {
    return DailyOrders(
      date: parseLocalDate(json['date'].toString()),
      orderCount: parseRequiredInt(json['orderCount'], 'orderCount'),
      totalOrderAmount: parseRequiredInt(json['totalOrderAmount'], 'totalOrderAmount'),
      totalFee: parseRequiredInt(json['totalFee'], 'totalFee'),
      totalTip: parseRequiredInt(json['totalTip'], 'totalTip'),
      totalNet: parseRequiredInt(json['totalNet'], 'totalNet'),
      orders: ((json['orders'] as List?) ?? const [])
          .map((e) => OrderModel.fromJson(Map<String, dynamic>.from(e as Map)))
          .toList(),
    );
  }
}
