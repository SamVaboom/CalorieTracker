package com.sam.caloriestreak.domain.achievement

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object AchievementEvaluator {
    fun consecutiveRecordedDays(meals: List<MealLogEntity>): Int {
        val days = meals.map { it.dateEpochDay }.distinct().sorted()
        if (days.isEmpty()) return 0
        var best = 1
        var current = 1
        for (index in 1 until days.size) {
            current = if (days[index] == days[index - 1] + 1) current + 1 else 1
            if (current > best) best = current
        }
        return best
    }

    fun eligibleTimeAchievements(meals: List<MealLogEntity>): List<AchievementDefinition> {
        val streak = consecutiveRecordedDays(meals)
        return AchievementRegistry.time.filter { streak >= it.requiredDays }.map { it.asAchievement() }
    }

    fun eligibleWeightAchievements(entries: List<WeightEntryEntity>, zone: ZoneId = ZoneId.systemDefault()): List<AchievementDefinition> {
        if (entries.isEmpty()) return emptyList()
        val sorted = entries.sortedBy { it.timestamp }
        val first = sorted.first().kilograms
        val minimum = sorted.minOf { it.kilograms }
        val distinctDates = sorted.map { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }.distinct().size
        val ids = buildSet {
            add("weight_first")
            if (distinctDates >= 7) add("weight_7_dates")
            if (distinctDates >= 30) add("weight_30_dates")
            if (first - minimum >= 1.0) add("weight_down_1")
            if (first - minimum >= 5.0) add("weight_down_5")
            if (first - minimum >= 10.0) add("weight_down_10")
            if (sorted.size >= 6 && sorted.drop(5).any { later -> later.kilograms < sorted.takeWhile { it.timestamp < later.timestamp }.minOf { it.kilograms } }) add("weight_new_low")
        }
        return AchievementRegistry.weight.filter { it.id in ids }
    }

    fun eligibleCalorieAndScoreAchievements(daily: List<DailyLogEntity>): Set<String> {
        val finalized = daily.filter { it.finalized }.sortedBy { it.dateEpochDay }
        val result = mutableSetOf<String>()
        if (finalized.any { it.totalCalories < 1000 }) result += "fasting_monk"
        if (finalized.any { it.totalCalories < 800 }) result += "air_diet"
        if (finalized.any { it.totalCalories > 3000 }) result += "the_american"
        if (finalized.any { it.totalCalories > 4000 }) result += "thanksgiving"
        if (finalized.any { it.totalCalories > 5000 }) result += "bottomless_pit"
        if (finalized.any { kotlin.math.abs(it.score - 100.0) < 0.000001 }) result += "bullseye"
        if (finalized.count { it.score >= 80.0 } >= 30) result += "safe_passage"
        val perfectRuns = consecutiveRuns(finalized.filter { kotlin.math.abs(it.score - 100.0) < 0.000001 }.map { it.dateEpochDay })
        if (perfectRuns >= 2) result += "double_bullseye"
        if (perfectRuns >= 3) result += "hat_trick"
        if (consecutiveRuns(finalized.filter { it.score >= 85.0 }.map { it.dateEpochDay }) >= 7) result += "balanced_week"
        if (finalized.windowed(7).any { window -> window.count { it.totalCalories < 1000 } >= 3 && window.last().dateEpochDay - window.first().dateEpochDay <= 6 }) result += "enlightened_monk"
        return result
    }

    private fun consecutiveRuns(days: List<Long>): Int {
        if (days.isEmpty()) return 0
        val sorted = days.distinct().sorted()
        var best = 1
        var current = 1
        for (index in 1 until sorted.size) {
            current = if (sorted[index] == sorted[index - 1] + 1) current + 1 else 1
            best = maxOf(best, current)
        }
        return best
    }
}
