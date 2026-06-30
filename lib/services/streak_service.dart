import '../models/app_settings.dart';
import '../models/logs.dart';
import 'score_calculator.dart';

class StreakResult {
  const StreakResult({required this.current, required this.best, required this.freezes, required this.goodProgress, required this.freezeUses, required this.events});
  final int current;
  final int best;
  final int freezes;
  final int goodProgress;
  final int freezeUses;
  final List<AccountabilityEvent> events;
}

class StreakService {
  const StreakService();

  StreakResult calculate(Map<DateTime, double> caloriesByDay, AppSettings settings) {
    final dates = caloriesByDay.keys.toList()..sort();
    var current = 0, best = 0, freezes = 0, good = 0, used = 0;
    final events = <AccountabilityEvent>[];
    const scoreCalc = ScoreCalculator();

    for (final date in dates) {
      final calories = caloriesByDay[date] ?? 0;
      final score = scoreCalc.scoreForCalories(calories, settings);
      if (score >= settings.streakThreshold) {
        current++;
        if (score >= settings.freezeThreshold) good++;
        if (good >= settings.goodDaysForFreeze) {
          good = 0;
          if (freezes < settings.maxFreezes) freezes++;
        }
      } else if (settings.autoUseFreezes && freezes > 0) {
        freezes--;
        used++;
        current++;
      } else {
        final previous = current;
        current = 0;
        events.add(AccountabilityEvent(date: date, calories: calories, score: score, previousStreak: previous, message: 'Missed target. Calories: ${calories.round()} kcal. Score: ${score.round()}%. Previous streak: $previous days.'));
      }
      if (current > best) best = current;
    }
    return StreakResult(current: current, best: best, freezes: freezes, goodProgress: good, freezeUses: used, events: events);
  }
}
