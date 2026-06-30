import 'package:flutter/material.dart';

import '../models/app_settings.dart';
import '../services/score_calculator.dart';

class DashboardPage extends StatelessWidget {
  const DashboardPage({super.key});

  @override
  Widget build(BuildContext context) {
    final settings = AppSettings.defaults();
    final score = const ScoreCalculator().scoreForCalories(1650, settings);
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Text('Dashboard', style: Theme.of(context).textTheme.headlineSmall),
        Card(child: ListTile(title: const Text('Today'), subtitle: Text('1650 kcal · ${score.round()}%'))),
        Card(child: ListTile(title: const Text('Target'), subtitle: Text('${settings.target.round()} kcal'))),
      ],
    );
  }
}
