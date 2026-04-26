import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/date_symbol_data_local.dart';

import 'app.dart';
import 'core/local_storage.dart';
import 'core/providers.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initializeDateFormatting('vi_VN');
  final storage = await LocalStorage.create();

  runApp(
    ProviderScope(
      overrides: [
        localStorageProvider.overrideWithValue(storage),
      ],
      child: const TaxiIncomeApp(),
    ),
  );
}
