import '../models/app_settings.dart';

class ScoreCalculator {
  const ScoreCalculator();

  double scoreForCalories(double calories, AppSettings settings) {
    final points = [...settings.curve]..sort((a, b) => a.calories.compareTo(b.calories));
    if (calories <= points.first.calories) return points.first.score;
    if (calories >= points.last.calories) return points.last.score;
    for (var i = 0; i < points.length - 1; i++) {
      final a = points[i];
      final b = points[i + 1];
      if (calories >= a.calories && calories <= b.calories) {
        final t = (calories - a.calories) / (b.calories - a.calories);
        return (a.score + t * (b.score - a.score)).clamp(0, 100).toDouble();
      }
    }
    return 0;
  }

  String label(double score) => score >= 95 ? 'perfect' : score >= 85 ? 'good' : score >= 80 ? 'okay' : 'bad';
}
