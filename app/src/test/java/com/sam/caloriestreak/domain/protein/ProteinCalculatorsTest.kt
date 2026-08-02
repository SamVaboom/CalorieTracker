package com.sam.caloriestreak.domain.protein

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.domain.history.HistoryRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProteinCalculatorsTest {
    private fun ingredient(
        id: String,
        protein: Double?,
        referenceAmount: Double = 100.0,
        unit: String = "g",
        archived: Boolean = false
    ) = IngredientEntity(
        id = id,
        name = id,
        calories = 100.0,
        referenceAmount = referenceAmount,
        referenceUnit = unit,
        proteinPerReferenceAmount = protein,
        archived = archived,
        createdAt = 1,
        updatedAt = 1
    )

    private fun item(id: String, ingredientId: String, amount: Double, unit: String) = RecipeItemEntity(
        id = id,
        recipeId = "recipe",
        ingredientId = ingredientId,
        ingredientName = ingredientId,
        amount = amount,
        unit = unit
    )

    private fun meal(
        id: String,
        day: Long,
        protein: Double?,
        complete: Boolean = protein != null,
        missing: Int = if (protein == null) 1 else 0
    ) = MealLogEntity(
        id = id,
        dateEpochDay = day,
        timeMillis = day * 86_400_000L,
        recipeName = id,
        portionDescription = "1 serving",
        portionMultiplier = 1.0,
        calories = 500.0,
        proteinGramsSnapshot = protein,
        proteinDataComplete = complete,
        missingProteinItemCount = missing,
        createdAt = 1,
        updatedAt = 1
    )

    @Test fun proportionalProteinSupportsGramsAndFractions() {
        assertEquals(66.0, IngredientProteinCalculator.grams(22.0, 100.0, 300.0)!!, 0.0001)
        assertEquals(5.5, IngredientProteinCalculator.grams(22.0, 100.0, 25.0)!!, 0.0001)
    }

    @Test fun proportionalProteinSupportsMillilitresAndPieces() {
        assertEquals(3.0, IngredientProteinCalculator.grams(ingredient("milk", 3.0, 100.0, "ml"), 100.0, "ml")!!, 0.0001)
        assertEquals(3.9, IngredientProteinCalculator.grams(ingredient("banana", 1.3, 1.0, "piece"), 3.0, "pieces")!!, 0.0001)
    }

    @Test fun missingProteinStaysUnknownWhileExplicitZeroIsKnown() {
        assertNull(IngredientProteinCalculator.grams(null, 100.0, 300.0))
        assertEquals(0.0, IngredientProteinCalculator.grams(0.0, 100.0, 300.0)!!, 0.0001)
    }

    @Test fun recipeReportsPartiallyKnownAndFullyKnownTotals() {
        val items = listOf(item("one", "cheese", 300.0, "g"), item("two", "sauce", 200.0, "g"))
        val partial = RecipeProteinCalculator.calculate(
            items,
            mapOf("cheese" to ingredient("cheese", 22.0), "sauce" to ingredient("sauce", null))
        )
        assertEquals(66.0, partial.knownGrams, 0.0001)
        assertFalse(partial.complete)
        assertEquals(1, partial.missingIngredientCount)
        assertNull(partial.perServing(2.0))

        val complete = RecipeProteinCalculator.calculate(
            items,
            mapOf("cheese" to ingredient("cheese", 22.0), "sauce" to ingredient("sauce", 1.2))
        )
        assertEquals(68.4, complete.knownGrams, 0.0001)
        assertTrue(complete.complete)
        assertEquals(34.2, complete.perServing(2.0)!!, 0.0001)
    }

    @Test fun dailyProteinKeepsKnownTotalAndMissingCount() {
        val summary = DailyProteinCalculator.calculate(listOf(meal("known", 10, 94.2), meal("unknown", 10, null)))
        assertEquals(94.2, summary.knownGrams, 0.0001)
        assertFalse(summary.complete)
        assertEquals(1, summary.missingCount)
        assertTrue(summary.hasKnownData)
    }

    @Test fun proteinHistoryDoesNotTurnUnknownOrMissingDatesIntoZero() {
        val points = ProteinHistorySeriesBuilder.build(
            meals = listOf(meal("known", 10, 50.0), meal("unknown", 11, null), meal("explicit-zero", 12, 0.0)),
            range = HistoryRange.ALL,
            todayEpochDay = 12
        )
        assertEquals(listOf(10L, 12L), points.map { it.epochDay })
        assertEquals(0.0, points.last().knownGrams, 0.0001)
    }

    @Test fun statisticsAverageOnlyRecordedDaysAndCountsIngredientCoverage() {
        val stats = ProteinStatisticsCalculator.calculate(
            meals = listOf(meal("a", 100, 100.0), meal("b", 98, 50.0), meal("unknown", 99, null)),
            ingredients = listOf(ingredient("assigned", 0.0), ingredient("missing", null), ingredient("archived", null, archived = true)),
            todayEpochDay = 100
        )
        assertEquals(150.0, stats.last7Days.knownTotalGrams, 0.0001)
        assertEquals(3, stats.last7Days.recordedDayCount)
        assertEquals(50.0, stats.last7Days.knownAveragePerRecordedDay, 0.0001)
        assertEquals(100.0, stats.highestKnownDayGrams!!, 0.0001)
        assertEquals(100.0, stats.highestKnownMealGrams!!, 0.0001)
        assertEquals(1, stats.daysAtLeast100Grams)
        assertEquals(1, stats.activeIngredientsAssigned)
        assertEquals(1, stats.activeIngredientsMissing)
        assertEquals(50.0, stats.activeIngredientsAssignedPercent, 0.0001)
        assertTrue(stats.historicalDataIncomplete)
    }
}
