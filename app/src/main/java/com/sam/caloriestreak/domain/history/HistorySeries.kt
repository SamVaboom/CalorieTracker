package com.sam.caloriestreak.domain.history

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.domain.calculation.ScoreCalculator

enum class HistoryMetric(val label: String) {
    SCORE("Score %"),
    CALORIES("Calories")
}

enum class HistoryRange(val label: String, val dayCount: Long?) {
    WEEK("Last week", 7),
    MONTH("Last month", 30),
    YEAR("Last year", 365),
    ALL("All time", null)
}

object HistoryGraphDefaults {
    val metric = HistoryMetric.SCORE
    val range = HistoryRange.WEEK
}

data class HistoryPoint(
    val epochDay: Long,
    val calories: Double,
    val score: Double,
    val freezeProtected: Boolean,
    val hasMealData: Boolean
) {
    fun value(metric: HistoryMetric): Double = when (metric) {
        HistoryMetric.SCORE -> score
        HistoryMetric.CALORIES -> calories
    }
}

object HistorySeriesBuilder {
    fun build(
        meals: List<MealLogEntity>,
        dailyLogs: List<DailyLogEntity>,
        range: HistoryRange,
        todayEpochDay: Long,
        scoreCalculator: ScoreCalculator = ScoreCalculator()
    ): List<HistoryPoint> {
        val mealsByDay = meals.groupBy { it.dateEpochDay }
        val dailyByDay = dailyLogs.associateBy { it.dateEpochDay }
        val firstStoredDay = listOfNotNull(
            meals.minOfOrNull { it.dateEpochDay },
            dailyLogs.minOfOrNull { it.dateEpochDay }
        ).minOrNull()

        val firstDay = when (val count = range.dayCount) {
            null -> firstStoredDay ?: return emptyList()
            else -> todayEpochDay - count + 1
        }
        if (firstDay > todayEpochDay) return emptyList()

        return (firstDay..todayEpochDay).map { day ->
            val dayMeals = mealsByDay[day].orEmpty()
            val calories = dayMeals.sumOf { it.calories }
            val daily = dailyByDay[day]
            HistoryPoint(
                epochDay = day,
                calories = calories,
                score = daily?.score ?: scoreCalculator.calculate(calories),
                freezeProtected = daily?.freezeUsed == true || daily?.manualCheatDay == true,
                hasMealData = dayMeals.isNotEmpty()
            )
        }
    }

    fun hasTrackedData(points: List<HistoryPoint>, dailyLogs: List<DailyLogEntity>): Boolean {
        if (points.isEmpty()) return false
        val days = points.mapTo(mutableSetOf()) { it.epochDay }
        return points.any { it.hasMealData } || dailyLogs.any { it.epochDay in days }
    }
}