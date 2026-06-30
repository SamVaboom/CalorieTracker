import 'score_point.dart';

class AppSettings {
  const AppSettings({required this.target, required this.curve, required this.streakThreshold, required this.freezeThreshold, required this.goodDaysForFreeze, required this.maxFreezes, this.autoUseFreezes = true});

  final double target;
  final List<ScorePoint> curve;
  final double streakThreshold;
  final double freezeThreshold;
  final int goodDaysForFreeze;
  final int maxFreezes;
  final bool autoUseFreezes;

  factory AppSettings.defaults() => const AppSettings(
        target: 1650,
        curve: [
          ScorePoint(calories: 800, score: 0),
          ScorePoint(calories: 1200, score: 40),
          ScorePoint(calories: 1400, score: 80),
          ScorePoint(calories: 1650, score: 100),
          ScorePoint(calories: 1800, score: 75),
          ScorePoint(calories: 2000, score: 20),
          ScorePoint(calories: 2200, score: 0),
        ],
        streakThreshold: 80,
        freezeThreshold: 85,
        goodDaysForFreeze: 5,
        maxFreezes: 3,
      );

  AppSettings copyWith({double? target, List<ScorePoint>? curve, double? streakThreshold, double? freezeThreshold, int? goodDaysForFreeze, int? maxFreezes, bool? autoUseFreezes}) => AppSettings(
        target: target ?? this.target,
        curve: curve ?? this.curve,
        streakThreshold: streakThreshold ?? this.streakThreshold,
        freezeThreshold: freezeThreshold ?? this.freezeThreshold,
        goodDaysForFreeze: goodDaysForFreeze ?? this.goodDaysForFreeze,
        maxFreezes: maxFreezes ?? this.maxFreezes,
        autoUseFreezes: autoUseFreezes ?? this.autoUseFreezes,
      );
}
