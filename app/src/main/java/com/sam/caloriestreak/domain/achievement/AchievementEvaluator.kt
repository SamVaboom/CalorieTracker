package com.sam.caloriestreak.domain.achievement

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

object AchievementEvaluator {
    fun consecutiveRecordedDays(meals: List<MealLogEntity>): Int {
        val days = meals.map { it.dateEpochDay }.distinct().sorted()
        return consecutiveRuns(days)
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
            if (sorted.size >= 6 && sorted.drop(5).any { later ->
                    val earlier = sorted.filter { it.timestamp < later.timestamp }
                    earlier.isNotEmpty() && later.kilograms < earlier.minOf { it.kilograms }
                }) add("weight_new_low")
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
        if (finalized.any { abs(it.score - 100.0) < 0.000001 }) result += "bullseye"
        if (finalized.count { it.score >= 80.0 } >= 30) result += "safe_passage"
        val perfectRuns = consecutiveRuns(finalized.filter { abs(it.score - 100.0) < 0.000001 }.map { it.dateEpochDay })
        if (perfectRuns >= 2) result += "double_bullseye"
        if (perfectRuns >= 3) result += "hat_trick"
        if (consecutiveRuns(finalized.filter { it.score >= 85.0 }.map { it.dateEpochDay }) >= 7) result += "balanced_week"
        val underThousand = finalized.filter { it.totalCalories < 1000 }.map { it.dateEpochDay }
        if (underThousand.any { end -> underThousand.count { it in (end - 6)..end } >= 3 }) result += "enlightened_monk"
        return result
    }

    fun eligibleMealAndRecipeAchievements(
        meals: List<MealLogEntity>,
        recipeCount: Int,
        ingredientCount: Int
    ): Set<String> {
        val recipeMeals = meals.filter { it.recipeId != null }.sortedBy { it.dateEpochDay }
        val result = mutableSetOf<String>()
        val byRecipe = recipeMeals.groupBy { it.recipeId!! }
        if (byRecipe.values.any { logs -> logs.any { end -> logs.count { it.dateEpochDay in (end.dateEpochDay - 29)..end.dateEpochDay } >= 15 } }) result += "picky_eater"
        if (byRecipe.values.any { logs ->
                val dates = logs.map { it.dateEpochDay }.distinct()
                dates.any { end -> dates.count { it in (end - 13)..end } >= 7 }
            }) result += "creature_of_habit"
        if (byRecipe.values.any { logs -> consecutiveRuns(logs.map { it.dateEpochDay }) >= 3 }) result += "meal_prepper"
        val distinctRecipes = recipeMeals.mapNotNull { it.recipeId }.distinct().size
        if (distinctRecipes >= 25) result += "explorer"
        if (distinctRecipes >= 50) result += "culinary_tourist"
        if (recipeMeals.size >= 100) result += "home_cook"
        if (recipeCount >= 50) result += "master_chef"
        if (ingredientCount >= 100) result += "ingredient_collector"
        return result
    }

    fun eligibleFreezeAchievements(daily: List<DailyLogEntity>, currentFreezes: Int, maxFreezes: Int = 3): Set<String> {
        val result = mutableSetOf<String>()
        val earnedFreezes = daily.count { it.freezeQualifying } / 7
        val used = daily.count { it.freezeUsed }
        if (earnedFreezes >= 1) result += "winter_is_coming"
        if (earnedFreezes >= 1) result += "seven_good_days"
        if (currentFreezes >= maxFreezes) result += "cold_storage"
        if (daily.any { it.freezeUsed && it.score < 80.0 }) result += "close_call"
        if (used >= 10) result += "ice_king"
        if (used >= 25) result += "deep_freeze"
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
