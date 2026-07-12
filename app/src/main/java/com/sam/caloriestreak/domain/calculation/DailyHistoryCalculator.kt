package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity

/**
 * Rebuilds completed daily summaries from meal snapshots.
 *
 * Existing historical records are used only to preserve explicit user choices such as a manual
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
        // Include an in-progress record that has become historical since the previous app launch.
        // This is how a manually frozen today survives and becomes finalized tomorrow.
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
                // Qualification always follows the actual calorie-based score, even on a frozen day.
                freezeQualifying = calculated.score >= 85.0,
                createdAt = previous?.createdAt ?: calculated.createdAt,
                updatedAt = now
            )
        }
        return rebuilt
    }
}