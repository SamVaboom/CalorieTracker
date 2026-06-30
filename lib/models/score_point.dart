class ScorePoint {
  const ScorePoint({required this.calories, required this.score});

  final double calories;
  final double score;

  Map<String, dynamic> toJson() => {
        'calories': calories,
        'score': score,
      };

  factory ScorePoint.fromJson(Map<String, dynamic> json) => ScorePoint(
        calories: (json['calories'] as num).toDouble(),
        score: (json['score'] as num).toDouble(),
      );
}
