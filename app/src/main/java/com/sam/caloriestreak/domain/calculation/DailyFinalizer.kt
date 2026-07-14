package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.DailyLogEntity

object DailyFinalizer {
    fun finalizeDay(
        day: Long,
        totalCalories: Double,
        previousDays: List<DailyLogEntity>,
        targetCalories: Double = ScoreCalculator.DEFAULT_TARGET,
        automaticFreeze: Boolean = true,
        now: Long = System.currentTimeMillis()
    ): DailyLogEntity {
        val score = ScoreCalculator.forTarget(targetCalories).calculate(totalCalories)
        val before = StreakCalculator.calculate(previousDays)
        val success = score >= 80.0
        val qualifying = score >= 85.0
        val useFreeze = !success && automaticFreeze && before.freezes > 0
        return DailyLogEntity(
            dateEpochDay = day,
            totalCalories = totalCalories,
            score = score,
            finalized = true,
            streakSuccessful = success,
            freezeUsed = useFreeze,
            manualCheatDay = false,
            freezeQualifying = qualifying,
            targetCalories = targetCalories,
            scoreCurveVersion = 1,
            createdAt = now,
            updatedAt = now
        )
    }
}
