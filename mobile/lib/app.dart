import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'core/providers.dart';
import 'core/theme.dart';
import 'features/onboarding/onboarding_screen.dart';
import 'features/personal/personal_screen.dart';
import 'home_shell.dart';

class TaxiIncomeApp extends ConsumerWidget {
  const TaxiIncomeApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = GoRouter(
      initialLocation: '/',
      refreshListenable: _UserChangeNotifier(ref),
      redirect: (context, state) {
        final user = ref.read(currentUserProvider);
        final loc = state.matchedLocation;
        final goingOnboarding = loc == '/onboarding';
        if (user == null && !goingOnboarding) return '/onboarding';
        if (user != null && goingOnboarding) return '/';
        return null;
      },
      routes: [
        GoRoute(
          path: '/onboarding',
          builder: (_, __) => const OnboardingScreen(),
        ),
        GoRoute(
          path: '/',
          builder: (_, __) => const HomeShell(),
        ),
        GoRoute(
          path: '/personal',
          builder: (_, __) => const PersonalScreen(),
        ),
      ],
    );

    return MaterialApp.router(
      title: 'Taxi Income',
      theme: buildAppTheme(),
      debugShowCheckedModeBanner: false,
      routerConfig: router,
      supportedLocales: const [Locale('vi', 'VN'), Locale('en', 'US')],
    );
  }
}

class _UserChangeNotifier extends ChangeNotifier {
  _UserChangeNotifier(this.ref) {
    ref.listen(currentUserProvider, (_, __) => notifyListeners());
  }
  final WidgetRef ref;
}
