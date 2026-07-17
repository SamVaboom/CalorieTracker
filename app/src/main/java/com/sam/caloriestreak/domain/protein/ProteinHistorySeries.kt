package com.sam.caloriestreak.domain.protein

import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.domain.history.HistoryRange

data class ProteinHistoryPoint(
    val epochDay: Long,
    val knownGrams: Double,
    val complete: Boolean,
    val missingEntryCount: Int
)

object ProteinHistorySeriesBuilder {
    fun build(
        meals: List<MealLogEntity>,
        range: HistoryRange,
        todayEpochDay: Long
    ): List<ProteinHistoryPoint> {
        val cutoff = range.dayCount?.let { todayEpochDay - it + 1 }
        return meals.asSequence()
            .filter { cutoff == null || it.dateEpochDay >= cutoff }
            .filter { it.dateEpochDay <= todayEpochDay }
            .groupBy { it.dateEpochDay }
            .mapNotNull { (day, dayMeals) ->
                val summary = DailyProteinCalculator.calculate(dayMeals)
                // Old unknown-only dates are omitted rather than plotted as a false zero.
                if (!summary.hasKnownData) null else ProteinHistoryPoint(
                    epochDay = day,
                    knownGrams = summary.knownGrams,
                    complete = summary.complete,
                    missingEntryCount = summary.missingCount
                )
            }
            .sortedBy { it.epochDay }
    }
}
