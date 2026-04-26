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
      orderAmount: (json['orderAmount'] as num).toInt(),
      feeRate: (json['feeRate'] as num).toDouble(),
      feeAmount: (json['feeAmount'] as num).toInt(),
      tipAmount: (json['tipAmount'] as num).toInt(),
      taxiCount: (json['taxiCount'] as num).toInt(),
      subtotal: (json['subtotal'] as num).toInt(),
      netAmount: (json['netAmount'] as num).toInt(),
      orderDate: DateTime.parse(json['orderDate'] as String),
      orderTime: (json['orderTime'] as String).substring(0, 5),
      note: json['note'] as String?,
      sourceType: (json['sourceType'] as String?) ?? 'MANUAL',
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
      date: DateTime.parse(json['date'] as String),
      orderCount: (json['orderCount'] as num).toInt(),
      totalOrderAmount: (json['totalOrderAmount'] as num).toInt(),
      totalFee: (json['totalFee'] as num).toInt(),
      totalTip: (json['totalTip'] as num).toInt(),
      totalNet: (json['totalNet'] as num).toInt(),
      orders: ((json['orders'] as List?) ?? const [])
          .map((e) => OrderModel.fromJson(Map<String, dynamic>.from(e as Map)))
          .toList(),
    );
  }
}
