package com.sam.caloriestreak.domain.protein

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import java.time.LocalDate

data class ProteinRangeStatistics(
    val knownTotalGrams: Double,
    val recordedDayCount: Int,
    val knownAveragePerRecordedDay: Double
)

data class ProteinStatistics(
    val today: ProteinSummary,
    val last7Days: ProteinRangeStatistics,
    val last30Days: ProteinRangeStatistics,
    val lastYear: ProteinRangeStatistics,
    val allTime: ProteinRangeStatistics,
    val highestKnownDayGrams: Double?,
    val highestKnownMealGrams: Double?,
    val daysAtLeast100Grams: Int,
    val activeIngredientsAssigned: Int,
    val activeIngredientsMissing: Int,
    val activeIngredientsAssignedPercent: Double,
    val historicalDataIncomplete: Boolean
)

object ProteinStatisticsCalculator {
    fun calculate(
        meals: List<MealLogEntity>,
        ingredients: List<IngredientEntity>,
        todayEpochDay: Long = LocalDate.now().toEpochDay()
    ): ProteinStatistics {
        val byDay = meals.groupBy { it.dateEpochDay }
        val summaries = byDay.mapValues { (_, dayMeals) -> DailyProteinCalculator.calculate(dayMeals) }
        val activeIngredients = ingredients.filterNot { it.archived }
        val assigned = activeIngredients.count { it.proteinPerReferenceAmount != null }
        val missing = activeIngredients.size - assigned

        fun range(days: Long?): ProteinRangeStatistics {
            val cutoff = days?.let { todayEpochDay - it + 1 }
            val selected = summaries.filterKeys { day -> day <= todayEpochDay && (cutoff == null || day >= cutoff) }
            val total = selected.values.sumOf { it.knownGrams }
            return ProteinRangeStatistics(
                knownTotalGrams = total,
                recordedDayCount = selected.size,
                knownAveragePerRecordedDay = if (selected.isEmpty()) 0.0 else total / selected.size
            )
        }

        return ProteinStatistics(
            today = summaries[todayEpochDay] ?: ProteinSummary(0.0, false, 0, false),
            last7Days = range(7),
            last30Days = range(30),
            lastYear = range(365),
            allTime = range(null),
            highestKnownDayGrams = summaries.values.filter { it.hasKnownData }.maxOfOrNull { it.knownGrams },
            highestKnownMealGrams = meals.mapNotNull { it.proteinGramsSnapshot }.maxOrNull(),
            daysAtLeast100Grams = summaries.values.count { it.knownGrams >= 100.0 },
            activeIngredientsAssigned = assigned,
            activeIngredientsMissing = missing,
            activeIngredientsAssignedPercent = if (activeIngredients.isEmpty()) 0.0 else assigned * 100.0 / activeIngredients.size,
            historicalDataIncomplete = meals.any { !it.proteinDataComplete || it.proteinGramsSnapshot == null }
        )
    }
}
