import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'features/dashboard/dashboard_screen.dart';
import 'features/orders/order_entry_screen.dart';
import 'features/orders/order_history_screen.dart';
import 'features/schedule/schedule_screen.dart';

class HomeShell extends ConsumerStatefulWidget {
  const HomeShell({super.key});

  @override
  ConsumerState<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends ConsumerState<HomeShell> {
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    final pages = <Widget>[
      DashboardScreen(onOpenPersonal: () => context.push('/personal')),
      const OrderEntryScreen(),
      const OrderHistoryScreen(),
      const ScheduleScreen(),
    ];

    return Scaffold(
      body: IndexedStack(index: _index, children: pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(
              icon: Icon(Icons.home_outlined),
              selectedIcon: Icon(Icons.home_rounded),
              label: 'Tổng quan'),
          NavigationDestination(
              icon: Icon(Icons.add_circle_outline_rounded),
              selectedIcon: Icon(Icons.add_circle_rounded),
              label: 'Nhập đơn'),
          NavigationDestination(
              icon: Icon(Icons.description_outlined),
              selectedIcon: Icon(Icons.description_rounded),
              label: 'Lịch sử'),
          NavigationDestination(
              icon: Icon(Icons.calendar_month_outlined),
              selectedIcon: Icon(Icons.calendar_month_rounded),
              label: 'Lịch tuần'),
        ],
      ),
    );
  }
}
