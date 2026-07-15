package com.sam.caloriestreak.domain.achievement

import com.sam.caloriestreak.data.local.entity.ActivityEventEntity
import com.sam.caloriestreak.data.local.entity.ActivityEventType
import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

object AchievementEvaluator {
    fun consecutiveRecordedDays(meals: List<MealLogEntity>): Int = consecutiveRuns(meals.map { it.dateEpochDay })

    fun eligibleTimeAchievements(meals: List<MealLogEntity>): List<AchievementDefinition> {
        val streak = consecutiveRecordedDays(meals)
        return AchievementRegistry.time.filter { streak >= it.requiredDays }.map { it.asAchievement() }
    }

    fun eligibleWeightAchievements(
        entries: List<WeightEntryEntity>,
        weightGoal: Double?,
        zone: ZoneId = ZoneId.systemDefault()
    ): Set<String> {
        if (entries.isEmpty()) return emptySet()
        val sorted = entries.sortedBy { it.timestamp }
        val first = sorted.first().kilograms
        val latest = sorted.last().kilograms
        val loss = first - latest
        val distinctDates = sorted.map { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }.distinct().size
        return buildSet {
            add("weight_first_entry")
            if (distinctDates >= 7) add("weight_7_dates")
            if (distinctDates >= 30) add("weight_30_dates")
            (1..19).forEach { kilograms ->
                if (loss >= kilograms) add("weight_loss_${kilograms.toString().padStart(2, '0')}")
            }
            if (sorted.size >= 2 && latest < sorted.dropLast(1).minOf { it.kilograms }) add("weight_phoenix")
            if (weightGoal != null && weightGoal < first) {
                val requiredLoss = first - weightGoal
                if (loss >= requiredLoss / 2.0) add("weight_halfway")
                if (latest <= weightGoal) add("weight_goal")
            }
        }
    }

    fun eligibleCalorieAndScoreAchievements(daily: List<DailyLogEntity>): Set<String> {
        val finalized = daily.filter { it.finalized }.sortedBy { it.dateEpochDay }
        val result = mutableSetOf<String>()
        if (finalized.any { it.totalCalories < 1000 }) result += "fasting_monk"
        if (finalized.any { it.totalCalories < 800 }) result += "air_diet"
        if (finalized.any { abs(it.totalCalories) < 0.0001 }) result += "ghost"
        if (finalized.any { it.totalCalories > 3000 }) result += "the_american"
        if (finalized.any { it.totalCalories > 4000 }) result += "thanksgiving"
        if (finalized.any { it.totalCalories > 5000 }) result += "bottomless_pit"
        if (finalized.any { it.manualCheatDay && it.totalCalories > 2500 }) result += "cheat_code"
        if (finalized.any { it.totalCalories > it.targetCalories && it.score >= 80.0 }) result += "testing_limits"
        if (consecutiveRuns(finalized.filter { it.totalCalories > it.targetCalories && it.score >= 80.0 }.map { it.dateEpochDay }) >= 3) result += "loophole"
        if (finalized.any { it.totalCalories > 2200 && !it.freezeUsed }) result += "no_regrets"
        if (finalized.any { abs(it.totalCalories - 1666.0) < 0.5 }) result += "lucky_seven"

        if (finalized.any { isPerfect(it.score) }) result += "bullseye"
        val perfectRun = consecutiveRuns(finalized.filter { isPerfect(it.score) }.map { it.dateEpochDay })
        if (perfectRun >= 3) result += "three_in_a_row"
        if (perfectRun >= 7) result += "perfectionist"
        if (consecutiveRuns(finalized.filter { it.score in 95.0..100.0 }.map { it.dateEpochDay }) >= 14) result += "master_of_balance"

        val lowDays = finalized.filter { it.totalCalories < 1000 }.map { it.dateEpochDay }
        if (lowDays.any { end -> lowDays.count { it in (end - 6)..end } >= 3 }) result += "enlightened_monk"
        return result
    }

    fun eligibleMealAndRecipeAchievements(
        meals: List<MealLogEntity>,
        recipeCount: Int,
        ingredientCount: Int,
        zone: ZoneId = ZoneId.systemDefault()
    ): Set<String> {
        val recipeMeals = meals.filter { it.recipeId != null }.sortedBy { it.timeMillis }
        val manualMeals = meals.filter { it.recipeId == null }
        val result = mutableSetOf<String>()
        val byRecipe = recipeMeals.groupBy { it.recipeId!! }

        if (byRecipe.values.any { logs -> logs.any { end -> logs.count { it.dateEpochDay in (end.dateEpochDay - 29)..end.dateEpochDay } >= 15 } }) result += "picky_eater"
        val breakfastByRecipe = recipeMeals.filter {
            Instant.ofEpochMilli(it.timeMillis).atZone(zone).hour in 4..10
        }.groupBy { it.recipeId!! }
        if (breakfastByRecipe.values.any { it.size >= 20 }) result += "creature_of_habit"
        if (byRecipe.values.any { logs -> consecutiveRuns(logs.map { it.dateEpochDay }) >= 3 }) result += "meal_prep"
        if (recipeMeals.mapNotNull { it.recipeId }.distinct().size >= 25) result += "explorer"
        if (recipeCount >= 25) result += "chef"
        if (recipeCount >= 100) result += "master_chef"
        if (ingredientCount >= 100) result += "ingredient_collector"
        if (recipeMeals.size >= 100) result += "home_cook"
        if (manualMeals.size >= 25) result += "freestyler"

        if (hasDuplicateWithinMinute(meals)) result += "oops"
        if (meals.any { Instant.ofEpochMilli(it.timeMillis).atZone(zone).hour in 0..3 }) result += "night_owl"
        val beforeSevenDays = meals.filter { Instant.ofEpochMilli(it.timeMillis).atZone(zone).hour < 7 }.map { it.dateEpochDay }
        if (consecutiveRuns(beforeSevenDays) >= 7) result += "breakfast_champion"
        if (hasLastMinuteMeal(meals, zone)) result += "last_minute"
        if (hasIdenticalConsecutiveDays(meals)) result += "deja_vu"
        return result
    }

    fun eligibleFreezeAchievements(
        daily: List<DailyLogEntity>,
        currentFreezes: Int,
        events: List<ActivityEventEntity>,
        maxFreezes: Int = 3
    ): Set<String> {
        val result = mutableSetOf<String>()
        val qualifying = daily.count { it.finalized && it.freezeQualifying }
        val used = daily.count { it.freezeUsed }
        if (qualifying >= 7) result += "winter_is_coming"
        if (currentFreezes >= maxFreezes) result += "cold_storage"
        if (daily.any { it.freezeUsed && it.score < 80.0 }) result += "close_call"
        if (used >= 10) result += "ice_king"
        if (events.any { it.type == ActivityEventType.LAST_FREEZE_USED }) result += "yolo"
        return result
    }

    fun eligibleActivityAchievements(events: List<ActivityEventEntity>): Set<String> = buildSet {
        if (events.count { it.type == ActivityEventType.GROCERY_GENERATED } >= 10) add("shopping_spree")
        if (events.any { it.type == ActivityEventType.GROCERY_COMPLETED }) add("organized")
        val openDays = events.filter { it.type == ActivityEventType.APP_OPEN }.map { it.epochDay }
        if (consecutiveRuns(openDays) >= 30) add("touch_grass")
    }

    private fun hasDuplicateWithinMinute(meals: List<MealLogEntity>): Boolean {
        val sorted = meals.sortedBy { it.timeMillis }
        return sorted.zipWithNext().any { (a, b) ->
            b.timeMillis - a.timeMillis <= 60_000L &&
                a.recipeId == b.recipeId &&
                a.recipeName == b.recipeName &&
                abs(a.calories - b.calories) < 0.01 &&
                abs(a.portionMultiplier - b.portionMultiplier) < 0.001
        }
    }

    private fun hasLastMinuteMeal(meals: List<MealLogEntity>, zone: ZoneId): Boolean =
        meals.groupBy { it.dateEpochDay }.values.any { dayMeals ->
            val last = dayMeals.maxByOrNull { it.timeMillis } ?: return@any false
            val time = Instant.ofEpochMilli(last.timeMillis).atZone(zone)
            time.hour == 23 && time.minute >= 50
        }

    private fun hasIdenticalConsecutiveDays(meals: List<MealLogEntity>): Boolean {
        val signatures = meals.groupBy { it.dateEpochDay }.mapValues { (_, logs) ->
            logs.sortedWith(compareBy<MealLogEntity> { it.recipeId ?: "" }.thenBy { it.recipeName }.thenBy { it.calories })
                .map { "${it.recipeId}|${it.recipeName}|${"%.2f".format(it.calories)}|${"%.3f".format(it.portionMultiplier)}" }
        }
        return signatures.keys.sorted().zipWithNext().any { (a, b) -> b == a + 1 && signatures[a] == signatures[b] }
    }

    private fun isPerfect(score: Double) = abs(score - 100.0) < 0.000001

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
