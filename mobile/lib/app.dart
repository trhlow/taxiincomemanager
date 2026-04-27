import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/app_router.dart';
import 'core/theme.dart';

class TaxiIncomeApp extends ConsumerWidget {
  const TaxiIncomeApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);

    return MaterialApp.router(
      title: 'Taxi Income',
      theme: buildAppTheme(),
      debugShowCheckedModeBanner: false,
      routerConfig: router,
      supportedLocales: const [Locale('vi', 'VN'), Locale('en', 'US')],
    );
  }
}
