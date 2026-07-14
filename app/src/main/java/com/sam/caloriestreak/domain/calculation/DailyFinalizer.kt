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
        val success = score >= StreakRules.STREAK_SCORE_THRESHOLD
        val qualifying = score >= StreakRules.FREEZE_QUALIFYING_SCORE
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
            createdAt = now,
            updatedAt = now,
            targetCalories = targetCalories,
            scoreCurveVersion = 1
        )
    }
}
