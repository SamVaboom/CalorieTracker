package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.MealLogEntity

data class CalorieRangeStatistics(val dailyTotals: List<Double>, val calendarDays: Int) {
    val recordedDays: Int get() = dailyTotals.size
    val averageCalories: Double? get() = dailyTotals.takeIf { it.isNotEmpty() }?.average()
    fun averageScore(calculator: ScoreCalculator): Double? =
        dailyTotals.takeIf { it.isNotEmpty() }?.map(calculator::calculate)?.average()
}

object CalorieStatisticsCalculator {
    fun completedDays(meals: List<MealLogEntity>, today: Long, days: Int): CalorieRangeStatistics {
        require(days > 0)
        // Epoch calendar days avoid daylight-saving duration errors. A logged zero is known;
        // an absent day (including auto-finalized empty days) is not a calorie observation.
        val totals = meals.filter { it.dateEpochDay >= today - days && it.dateEpochDay < today }
            .groupBy { it.dateEpochDay }.toSortedMap().values.map { day -> day.sumOf { it.calories } }
        return CalorieRangeStatistics(totals, days)
    }
}
