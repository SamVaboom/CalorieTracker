import 'package:flutter/material.dart';

import 'pages/dashboard_page.dart';
import 'pages/simple_page.dart';

void main() => runApp(const CalorieStreakApp());

class CalorieStreakApp extends StatefulWidget {
  const CalorieStreakApp({super.key});

  @override
  State<CalorieStreakApp> createState() => _CalorieStreakAppState();
}

class _CalorieStreakAppState extends State<CalorieStreakApp> {
  int index = 0;

  @override
  Widget build(BuildContext context) {
    final pages = const [
      DashboardPage(),
      SimplePage(title: 'Ingredients', body: 'Ingredient CRUD page placeholder.'),
      SimplePage(title: 'Recipes', body: 'Recipe CRUD page placeholder.'),
      SimplePage(title: 'Log Meal', body: 'Meal logging page placeholder.'),
      SimplePage(title: 'Grocery List', body: 'Grocery checklist page placeholder.'),
      SimplePage(title: 'Statistics', body: 'Basic statistics page placeholder.'),
      SimplePage(title: 'Settings', body: 'Settings page placeholder.'),
    ];
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(colorScheme: ColorScheme.fromSeed(seedColor: Colors.green), useMaterial3: true),
      home: Scaffold(
        appBar: AppBar(title: const Text('Calorie Streak')),
        body: pages[index],
        bottomNavigationBar: NavigationBar(
          selectedIndex: index,
          onDestinationSelected: (value) => setState(() => index = value),
          destinations: const [
            NavigationDestination(icon: Icon(Icons.dashboard), label: 'Dashboard'),
            NavigationDestination(icon: Icon(Icons.inventory), label: 'Ingredients'),
            NavigationDestination(icon: Icon(Icons.menu_book), label: 'Recipes'),
            NavigationDestination(icon: Icon(Icons.add_circle), label: 'Log'),
            NavigationDestination(icon: Icon(Icons.shopping_cart), label: 'Grocery'),
            NavigationDestination(icon: Icon(Icons.bar_chart), label: 'Stats'),
            NavigationDestination(icon: Icon(Icons.settings), label: 'Settings'),
          ],
        ),
      ),
    );
  }
}
