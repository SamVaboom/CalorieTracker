package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity

/**
 * Rebuilds completed daily summaries from meal snapshots.
 *
 * Existing finalized records are used only to preserve explicit user choices such as a manual
 * cheat day and their original creation timestamp. All calorie totals and score-derived fields
 * are recalculated chronologically so deleting an old meal cannot leave stale later streak state.
 */
object DailyHistoryCalculator {
    fun rebuildCompletedDays(
        meals: List<MealLogEntity>,
        existing: List<DailyLogEntity>,
        todayEpochDay: Long,
        automaticFreeze: Boolean = true,
        now: Long = System.currentTimeMillis()
    ): List<DailyLogEntity> {
        val completedMeals = meals.filter { it.dateEpochDay < todayEpochDay }
        val existingCompleted = existing.filter { it.finalized && it.dateEpochDay < todayEpochDay }
        val firstDay = listOfNotNull(
            completedMeals.minOfOrNull { it.dateEpochDay },
            existingCompleted.minOfOrNull { it.dateEpochDay }
        ).minOrNull() ?: return emptyList()

        val lastDay = todayEpochDay - 1
        if (firstDay > lastDay) return emptyList()

        val mealsByDay = completedMeals.groupBy { it.dateEpochDay }
        val existingByDay = existingCompleted.associateBy { it.dateEpochDay }
        val rebuilt = mutableListOf<DailyLogEntity>()

        for (day in firstDay..lastDay) {
            val previous = existingByDay[day]
            val total = mealsByDay[day].orEmpty().sumOf { it.calories }
            val calculated = DailyFinalizer.finalizeDay(
                day = day,
                totalCalories = total,
                previousDays = rebuilt,
                automaticFreeze = automaticFreeze,
                now = now
            )
            val manualCheatDay = previous?.manualCheatDay == true
            rebuilt += calculated.copy(
                freezeUsed = if (manualCheatDay) true else calculated.freezeUsed,
                manualCheatDay = manualCheatDay,
                createdAt = previous?.createdAt ?: calculated.createdAt,
                updatedAt = now
            )
        }
        return rebuilt
    }
}