package com.sam.caloriestreak.domain.calculation

import kotlin.math.abs

data class ScorePoint(val calories: Double, val score: Double)

class ScoreCalculator(points: List<ScorePoint> = defaultPoints) {
    private val curve = points.sortedBy { it.calories }
    private val targetPoint = curve.maxByOrNull { it.score }

    fun calculate(calories: Double): Double {
        require(curve.size >= 2) { "At least two score points are required" }
        targetPoint?.let { target ->
            if (abs(calories - target.calories) <= TARGET_EPSILON) return 100.0
        }
        if (calories <= curve.first().calories) return curve.first().score.coerceIn(0.0, 100.0)
        if (calories >= curve.last().calories) return curve.last().score.coerceIn(0.0, 100.0)
        val pair = curve.zipWithNext().first { (left, right) -> calories in left.calories..right.calories }
        val ratio = (calories - pair.first.calories) / (pair.second.calories - pair.first.calories)
        return (pair.first.score + ratio * (pair.second.score - pair.first.score)).coerceIn(0.0, 100.0)
    }

    fun status(score: Double): String = when {
        score < 40.0 -> "Bad"
        score < 80.0 -> "Poor"
        score < 85.0 -> "Streak safe"
        score < 95.0 -> "Good"
        else -> "Excellent"
    }

    companion object {
        private const val TARGET_EPSILON = 0.000001

        val defaultPoints = listOf(
            ScorePoint(800.0, 0.0),
            ScorePoint(1200.0, 40.0),
            ScorePoint(1400.0, 80.0),
            ScorePoint(1650.0, 100.0),
            ScorePoint(1800.0, 75.0),
            ScorePoint(2000.0, 20.0),
            ScorePoint(2200.0, 0.0)
        )
    }
}
