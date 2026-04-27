import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'core/providers.dart';
import 'core/theme.dart';
import 'features/onboarding/onboarding_screen.dart';
import 'features/personal/personal_screen.dart';
import 'home_shell.dart';

class TaxiIncomeApp extends ConsumerStatefulWidget {
  const TaxiIncomeApp({super.key});

  @override
  ConsumerState<TaxiIncomeApp> createState() => _TaxiIncomeAppState();
}

class _TaxiIncomeAppState extends ConsumerState<TaxiIncomeApp> {
  late final _UserChangeNotifier _userChanges;
  late final GoRouter _router;

  @override
  void initState() {
    super.initState();
    _userChanges = _UserChangeNotifier(ref);
    _router = GoRouter(
      initialLocation: '/',
      refreshListenable: _userChanges,
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
  }

  @override
  void dispose() {
    _userChanges.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Taxi Income',
      theme: buildAppTheme(),
      debugShowCheckedModeBanner: false,
      routerConfig: _router,
      supportedLocales: const [Locale('vi', 'VN'), Locale('en', 'US')],
    );
  }
}

class _UserChangeNotifier extends ChangeNotifier {
  _UserChangeNotifier(this.ref) {
    _sub = ref.listen<CurrentUser?>(
      currentUserProvider,
      (_, __) => notifyListeners(),
    );
  }

  final WidgetRef ref;
  ProviderSubscription<CurrentUser?>? _sub;

  @override
  void dispose() {
    _sub?.close();
    super.dispose();
  }
}
