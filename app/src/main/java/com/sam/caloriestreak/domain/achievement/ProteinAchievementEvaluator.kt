package com.sam.caloriestreak.domain.achievement

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import com.sam.caloriestreak.domain.protein.DailyProteinCalculator
import com.sam.caloriestreak.domain.protein.RecipeProteinCalculator
import java.time.Instant
import java.time.ZoneId

object ProteinAchievementEvaluator {
    fun eligibleAchievements(
        meals: List<MealLogEntity>,
        ingredients: List<IngredientEntity>,
        recipes: List<RecipeEntity>,
        recipeItems: List<RecipeItemEntity>,
        weights: List<WeightEntryEntity>,
        zone: ZoneId = ZoneId.systemDefault()
    ): Set<String> {
        val daySummaries = DailyProteinCalculator.byDay(meals)
        val knownDayTotals = daySummaries.mapValues { it.value.knownGrams }
        val knownMealProteins = meals.mapNotNull { it.proteinGramsSnapshot }
        val cumulativeKnown = knownMealProteins.sum()
        val activeIngredients = ingredients.filterNot { it.archived }
        val assignedIngredientCount = activeIngredients.count { it.proteinPerReferenceAmount != null }
        val result = mutableSetOf<String>()

        addThresholdAchievements(
            maximum = knownDayTotals.values.maxOrNull(),
            thresholds = listOf(
                50.0 to "protein_initiate",
                75.0 to "protein_solid_foundation",
                100.0 to "protein_triple_digits",
                125.0 to "protein_heavy_lifter",
                150.0 to "protein_150_club",
                200.0 to "protein_absolute_unit"
            ),
            result = result
        )
        addThresholdAchievements(
            maximum = knownMealProteins.maxOrNull(),
            thresholds = listOf(
                20.0 to "protein_snack",
                30.0 to "protein_power_meal",
                50.0 to "protein_main_course",
                75.0 to "protein_bomb"
            ),
            result = result
        )
        addThresholdAchievements(
            maximum = cumulativeKnown,
            thresholds = listOf(
                1_000.0 to "protein_kilo_club",
                5_000.0 to "protein_five_kilo_sack",
                10_000.0 to "protein_ten_kilo_plate",
                25_000.0 to "protein_warehouse",
                50_000.0 to "protein_industrial_quantities",
                100_000.0 to "protein_empire"
            ),
            result = result
        )

        val sortedWeights = weights.filter { it.kilograms > 0.0 }.sortedBy { it.timestamp }
        if (sortedWeights.isNotEmpty()) {
            val weightByDay = sortedWeights.map { entry ->
                Instant.ofEpochMilli(entry.timestamp).atZone(zone).toLocalDate().toEpochDay() to entry.kilograms
            }
            if (knownDayTotals.any { (day, grams) ->
                    val applicableWeight = weightByDay.lastOrNull { (weightDay, _) -> weightDay <= day }?.second
                    applicableWeight != null && grams >= applicableWeight
                }
            ) result += "protein_pound_for_pound"
            if (knownDayTotals.any { (day, grams) ->
                    val applicableWeight = weightByDay.lastOrNull { (weightDay, _) -> weightDay <= day }?.second
                    applicableWeight != null && grams >= applicableWeight * 2.0
                }
            ) result += "protein_double_density"

            val firstWeightKilograms = sortedWeights.first().kilograms
            if (cumulativeKnown >= firstWeightKilograms * 500.0) result += "protein_half_of_yourself"
            if (cumulativeKnown >= firstWeightKilograms * 1_000.0) result += "protein_eat_yourself"
        }

        val hundredGramDays = knownDayTotals.filterValues { it >= 100.0 }.keys.sorted()
        if (hundredGramDays.size >= 3) result += "protein_three_days"
        if (hundredGramDays.size >= 7) result += "protein_week"
        if (hundredGramDays.any { end -> hundredGramDays.count { it in (end - 29)..end } >= 20 }) {
            result += "protein_routine"
        }
        if (longestConsecutiveRun(hundredGramDays) >= 7) result += "protein_triple_digit_streak"

        if (assignedIngredientCount >= 25) result += "protein_detective"
        if (assignedIngredientCount >= 100) result += "protein_librarian"
        if (activeIngredients.size >= 25 && activeIngredients.all { it.proteinPerReferenceAmount != null }) {
            result += "protein_no_mystery_macros"
        }

        val completeDays = daySummaries.filterValues { it.complete }.keys.sorted()
        if (completeDays.isNotEmpty()) result += "protein_fully_calculated"
        if (longestConsecutiveRun(completeDays) >= 7) result += "protein_complete_week"

        val ingredientsById = ingredients.associateBy { it.id }
        val itemsByRecipe = recipeItems.groupBy { it.recipeId }
        val activeRecipeProteinPerServing = recipes.asSequence()
            .filterNot { it.archived }
            .mapNotNull { recipe ->
                val summary = RecipeProteinCalculator.calculate(itemsByRecipe[recipe.id].orEmpty(), ingredientsById)
                summary.perServing(recipe.servings)
            }
            .toList()
        if (activeRecipeProteinPerServing.count { it >= 20.0 } >= 10) result += "protein_chef"
        if (activeRecipeProteinPerServing.count { it >= 30.0 } >= 10) result += "protein_high_protein_menu"

        val qualifyingLoggedRecipes = meals.asSequence()
            .filter { it.recipeId != null }
            .filter { it.proteinDataComplete }
            .filter { (it.proteinGramsSnapshot ?: -1.0) >= 20.0 }
            .mapNotNull { it.recipeId }
            .distinct()
            .count()
        if (qualifyingLoggedRecipes >= 10) result += "protein_explorer"

        return result
    }

    private fun addThresholdAchievements(
        maximum: Double?,
        thresholds: List<Pair<Double, String>>,
        result: MutableSet<String>
    ) {
        if (maximum == null) return
        thresholds.forEach { (threshold, id) -> if (maximum >= threshold) result += id }
    }

    private fun longestConsecutiveRun(days: Collection<Long>): Int {
        val sorted = days.distinct().sorted()
        if (sorted.isEmpty()) return 0
        var current = 1
        var best = 1
        for (index in 1 until sorted.size) {
            current = if (sorted[index] == sorted[index - 1] + 1) current + 1 else 1
            best = maxOf(best, current)
        }
        return best
    }
}
