package com.sam.caloriestreak.domain.achievement

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProteinAchievementEvaluatorTest {
    private val zone = ZoneOffset.UTC

    private fun meal(
        id: String,
        day: Long,
        protein: Double?,
        complete: Boolean = protein != null,
        recipeId: String? = null
    ) = MealLogEntity(
        id = id,
        dateEpochDay = day,
        timeMillis = LocalDate.ofEpochDay(day).atStartOfDay(zone).toInstant().toEpochMilli(),
        recipeId = recipeId,
        recipeName = recipeId ?: id,
        portionDescription = "1 serving",
        portionMultiplier = 1.0,
        calories = 500.0,
        proteinGramsSnapshot = protein,
        proteinDataComplete = complete,
        missingProteinItemCount = if (complete) 0 else 1,
        createdAt = 1,
        updatedAt = 1
    )

    private fun ingredient(id: String, protein: Double?, archived: Boolean = false) = IngredientEntity(
        id = id,
        name = id,
        calories = 100.0,
        referenceAmount = 100.0,
        referenceUnit = "g",
        proteinPerReferenceAmount = protein,
        archived = archived,
        createdAt = 1,
        updatedAt = 1
    )

    private fun recipe(id: String) = RecipeEntity(id, id, servings = 1.0, createdAt = 1, updatedAt = 1)

    private fun recipeItem(recipeId: String, ingredientId: String) = RecipeItemEntity(
        id = "$recipeId-$ingredientId",
        recipeId = recipeId,
        ingredientId = ingredientId,
        ingredientName = ingredientId,
        amount = 100.0,
        unit = "g"
    )

    private fun weight(id: String, day: Long, kilograms: Double) = WeightEntryEntity(
        id = id,
        kilograms = kilograms,
        timestamp = LocalDate.ofEpochDay(day).atStartOfDay(zone).toInstant().toEpochMilli(),
        createdAt = 1,
        updatedAt = 1
    )

    private fun evaluate(
        meals: List<MealLogEntity> = emptyList(),
        ingredients: List<IngredientEntity> = emptyList(),
        recipes: List<RecipeEntity> = emptyList(),
        items: List<RecipeItemEntity> = emptyList(),
        weights: List<WeightEntryEntity> = emptyList()
    ) = ProteinAchievementEvaluator.eligibleAchievements(meals, ingredients, recipes, items, weights, zone)

    @Test fun dailyThresholdsUnlockAt50_75_100_125_150_200() {
        val ids = evaluate(meals = listOf(meal("day", 10, 200.0, complete = false)))
        listOf(
            "protein_initiate",
            "protein_solid_foundation",
            "protein_triple_digits",
            "protein_heavy_lifter",
            "protein_150_club",
            "protein_absolute_unit"
        ).forEach { assertTrue(it, it in ids) }
    }

    @Test fun incompleteDayQualifiesOnlyFromKnownAmount() {
        assertTrue("protein_triple_digits" in evaluate(meals = listOf(meal("known", 10, 105.0, false), meal("unknown", 10, null, false))))
        assertFalse("protein_triple_digits" in evaluate(meals = listOf(meal("known", 10, 99.9, false), meal("unknown", 10, null, false))))
    }

    @Test fun singleMealThresholdsUnlockAt20_30_50_75AndUnknownIsNeverAssumed() {
        val ids = evaluate(meals = listOf(meal("bomb", 10, 75.0)))
        listOf("protein_snack", "protein_power_meal", "protein_main_course", "protein_bomb").forEach { assertTrue(it in ids) }
        val unknown = evaluate(meals = listOf(meal("unknown", 10, null, false)))
        listOf("protein_snack", "protein_power_meal", "protein_main_course", "protein_bomb").forEach { assertFalse(it in unknown) }
    }

    @Test fun cumulativeThresholdsUnlockAt1k_5k_10k_25k_50k_100k() {
        val ids = evaluate(meals = listOf(meal("lifetime", 10, 100_000.0)))
        listOf(
            "protein_kilo_club",
            "protein_five_kilo_sack",
            "protein_ten_kilo_plate",
            "protein_warehouse",
            "protein_industrial_quantities",
            "protein_empire"
        ).forEach { assertTrue(it, it in ids) }
    }

    @Test fun deletedMealsNoLongerContributeToCurrentCumulativeEvidence() {
        val withMeal = evaluate(meals = listOf(meal("one-kilo", 10, 1_000.0)))
        val afterDelete = evaluate(meals = emptyList())
        assertTrue("protein_kilo_club" in withMeal)
        assertFalse("protein_kilo_club" in afterDelete)
    }

    @Test fun bodyWeightDailyUsesLatestWeightOnOrBeforeDayAndNeverFutureWeight() {
        val proteinDay = 20L
        val ids = evaluate(
            meals = listOf(meal("dense", proteinDay, 184.0)),
            weights = listOf(weight("old", 10, 92.0), weight("future", 30, 40.0))
        )
        assertTrue("protein_pound_for_pound" in ids)
        assertTrue("protein_double_density" in ids)

        val futureOnly = evaluate(
            meals = listOf(meal("earlier", proteinDay, 100.0)),
            weights = listOf(weight("future", 30, 50.0))
        )
        assertFalse("protein_pound_for_pound" in futureOnly)
    }

    @Test fun cumulativeBodyWeightUsesFirstChronologicalWeightAndRecalculatesAfterEdit() {
        val meals = listOf(meal("total", 20, 60_000.0))
        val first50 = evaluate(meals = meals, weights = listOf(weight("first", 10, 50.0), weight("later", 30, 90.0)))
        assertTrue("protein_half_of_yourself" in first50)
        assertTrue("protein_eat_yourself" in first50)

        val edited70 = evaluate(meals = meals, weights = listOf(weight("first", 10, 70.0), weight("later", 30, 90.0)))
        assertTrue("protein_half_of_yourself" in edited70)
        assertFalse("protein_eat_yourself" in edited70)
    }

    @Test fun consistencyAchievementsCoverThreeSevenRollingTwentyAndConsecutiveSeven() {
        val twentyDays = (1L..20L).map { day -> meal("day-$day", day, 100.0) }
        val ids = evaluate(meals = twentyDays)
        assertTrue("protein_three_days" in ids)
        assertTrue("protein_week" in ids)
        assertTrue("protein_routine" in ids)
        assertTrue("protein_triple_digit_streak" in ids)
    }

    @Test fun nonConsecutiveSevenDoesNotUnlockTripleDigitStreak() {
        val meals = listOf(1L, 3L, 5L, 7L, 9L, 11L, 13L).map { meal("day-$it", it, 100.0) }
        val ids = evaluate(meals = meals)
        assertTrue("protein_week" in ids)
        assertFalse("protein_triple_digit_streak" in ids)
    }

    @Test fun ingredientDataAchievementsTreatZeroAsAssignedAndNoMysteryAsDynamic() {
        val twentyFiveAssigned = (1..25).map { ingredient("i$it", if (it == 1) 0.0 else 10.0) }
        val earned = evaluate(ingredients = twentyFiveAssigned)
        assertTrue("protein_detective" in earned)
        assertTrue("protein_no_mystery_macros" in earned)

        val relocked = evaluate(ingredients = twentyFiveAssigned + ingredient("new-missing", null))
        assertFalse("protein_no_mystery_macros" in relocked)

        val hundred = (1..100).map { ingredient("hundred-$it", 1.0) }
        assertTrue("protein_librarian" in evaluate(ingredients = hundred))
    }

    @Test fun fullyCalculatedRequiresNonEmptyCompleteDayAndCompleteWeekRequiresSevenDays() {
        assertFalse("protein_fully_calculated" in evaluate())
        val week = (1L..7L).map { meal("complete-$it", it, 10.0, complete = true) }
        val ids = evaluate(meals = week)
        assertTrue("protein_fully_calculated" in ids)
        assertTrue("protein_complete_week" in ids)
        assertFalse("protein_complete_week" in evaluate(meals = week.dropLast(1) + meal("unknown", 7, null, false)))
    }

    @Test fun recipeAchievementsRequireFullyCalculableProteinPerServing() {
        val recipes = (1..10).map { recipe("recipe-$it") }
        val ingredient = ingredient("protein", 30.0)
        val items = recipes.map { recipeItem(it.id, ingredient.id) }
        val ids = evaluate(ingredients = listOf(ingredient), recipes = recipes, items = items)
        assertTrue("protein_chef" in ids)
        assertTrue("protein_high_protein_menu" in ids)

        val incomplete = evaluate(ingredients = listOf(ingredient("protein", null)), recipes = recipes, items = items)
        assertFalse("protein_chef" in incomplete)
        assertFalse("protein_high_protein_menu" in incomplete)
    }

    @Test fun proteinExplorerUsesStableRecipeIdsAndCompleteLoggedSnapshots() {
        val qualifying = (1..10).map { index -> meal("meal-$index", index.toLong(), 20.0, true, "recipe-$index") }
        assertTrue("protein_explorer" in evaluate(meals = qualifying))
        val sameRecipe = (1..10).map { index -> meal("same-$index", index.toLong(), 20.0, true, "one-recipe") }
        assertFalse("protein_explorer" in evaluate(meals = sameRecipe))
        val incomplete = qualifying.map { it.copy(proteinDataComplete = false) }
        assertFalse("protein_explorer" in evaluate(meals = incomplete))
    }
}
