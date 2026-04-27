import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../features/onboarding/onboarding_screen.dart';
import '../features/personal/personal_screen.dart';
import '../home_shell.dart';
import 'providers.dart';

/// Notifies [GoRouter] when [currentUserProvider] changes so redirect re-runs.
class _GoRouterRefresh extends ChangeNotifier {
  _GoRouterRefresh(this.ref) {
    _sub = ref.listen<CurrentUser?>(
      currentUserProvider,
      (_, __) => notifyListeners(),
    );
  }

  final Ref ref;
  ProviderSubscription<CurrentUser?>? _sub;

  @override
  void dispose() {
    _sub?.close();
    super.dispose();
  }
}

final _routerRefreshProvider = Provider<_GoRouterRefresh>((ref) {
  final notifier = _GoRouterRefresh(ref);
  ref.onDispose(notifier.dispose);
  return notifier;
});

/// Single [GoRouter] instance per [ProviderContainer]; survives widget rebuilds.
final routerProvider = Provider<GoRouter>((ref) {
  final refresh = ref.watch(_routerRefreshProvider);
  final router = GoRouter(
    initialLocation: '/',
    refreshListenable: refresh,
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
  ref.onDispose(router.dispose);
  return router;
});
