import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import 'api_client.dart';

/// User-facing message; expands [NETWORK_ERROR] with actionable copy.
String userFacingApiMessage(ApiException e) {
  if (e.code == 'NETWORK_ERROR') {
    return 'Không kết nối được máy chủ. Kiểm tra Wi‑Fi hoặc chỉnh Base URL tại '
        'mục Cá nhân (biểu tượng hồ sơ).';
  }
  return e.message;
}

void showApiErrorSnack(BuildContext context, ApiException e) {
  final msg = userFacingApiMessage(e);
  final showSettings = e.code == 'NETWORK_ERROR';
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(
      content: Text(msg),
      action: showSettings
          ? SnackBarAction(
              label: 'Cài đặt',
              onPressed: () => context.push('/personal'),
            )
          : null,
    ),
  );
}
