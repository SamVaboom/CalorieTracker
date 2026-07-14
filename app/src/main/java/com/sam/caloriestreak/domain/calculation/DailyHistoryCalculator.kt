package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity

object DailyHistoryCalculator {
    fun rebuildCompletedDays(
        meals: List<MealLogEntity>,
        existing: List<DailyLogEntity>,
        todayEpochDay: Long,
        configuredTarget: Double = ScoreCalculator.DEFAULT_TARGET,
        automaticFreeze: Boolean = true,
        now: Long = System.currentTimeMillis()
    ): List<DailyLogEntity> {
        val completedMeals = meals.filter { it.dateEpochDay < todayEpochDay }
        val existingHistorical = existing.filter { it.dateEpochDay < todayEpochDay }
        val firstDay = listOfNotNull(
            completedMeals.minOfOrNull { it.dateEpochDay },
            existingHistorical.minOfOrNull { it.dateEpochDay }
        ).minOrNull() ?: return emptyList()
        val lastDay = todayEpochDay - 1
        if (firstDay > lastDay) return emptyList()
        val mealsByDay = completedMeals.groupBy { it.dateEpochDay }
        val existingByDay = existingHistorical.associateBy { it.dateEpochDay }
        val rebuilt = mutableListOf<DailyLogEntity>()

        for (day in firstDay..lastDay) {
            val previous = existingByDay[day]
            val total = mealsByDay[day].orEmpty().sumOf { it.calories }
            val targetSnapshot = previous?.targetCalories ?: configuredTarget
            val calculated = DailyFinalizer.finalizeDay(
                day = day,
                totalCalories = total,
                previousDays = rebuilt,
                targetCalories = targetSnapshot,
                automaticFreeze = automaticFreeze,
                now = now
            )
            val manualCheatDay = previous?.manualCheatDay == true
            rebuilt += calculated.copy(
                freezeUsed = if (manualCheatDay) true else calculated.freezeUsed,
                manualCheatDay = manualCheatDay,
                freezeQualifying = calculated.score >= StreakRules.FREEZE_QUALIFYING_SCORE,
                targetCalories = targetSnapshot,
                scoreCurveVersion = previous?.scoreCurveVersion ?: 1,
                createdAt = previous?.createdAt ?: calculated.createdAt,
                updatedAt = now
            )
        }
        return rebuilt
    }
}
