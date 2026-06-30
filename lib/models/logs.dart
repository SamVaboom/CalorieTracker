class MealLog {
  const MealLog({required this.id, required this.date, required this.calories, this.recipeId, this.recipeName, this.fraction = 1, this.quick = false});
  final String id;
  final DateTime date;
  final double calories;
  final String? recipeId;
  final String? recipeName;
  final double fraction;
  final bool quick;
}

class DailyLog {
  const DailyLog({required this.date, this.manualFreezeUsed = false});
  final DateTime date;
  final bool manualFreezeUsed;
}

class AccountabilityEvent {
  const AccountabilityEvent({required this.date, required this.calories, required this.score, required this.previousStreak, required this.message});
  final DateTime date;
  final double calories;
  final double score;
  final int previousStreak;
  final String message;
}
