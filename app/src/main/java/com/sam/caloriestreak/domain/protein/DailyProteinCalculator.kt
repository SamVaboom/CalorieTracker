package com.sam.caloriestreak.domain.protein

import com.sam.caloriestreak.data.local.entity.MealLogEntity

object DailyProteinCalculator {
    fun calculate(meals: List<MealLogEntity>): ProteinSummary {
        val knownMeals = meals.filter { it.proteinGramsSnapshot != null }
        return ProteinSummary(
            knownGrams = knownMeals.sumOf { requireNotNull(it.proteinGramsSnapshot) },
            complete = meals.isNotEmpty() && meals.all { it.proteinDataComplete && it.proteinGramsSnapshot != null },
            missingCount = meals.count { !it.proteinDataComplete || it.proteinGramsSnapshot == null },
            hasKnownData = knownMeals.isNotEmpty()
        )
    }

    fun byDay(meals: List<MealLogEntity>): Map<Long, ProteinSummary> =
        meals.groupBy { it.dateEpochDay }.mapValues { (_, dayMeals) -> calculate(dayMeals) }
}
