package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.domain.editing.RecipeDraft
import org.junit.Assert.*
import org.junit.Test

class FlexibleMealCalculatorTest {
    private val rice = IngredientEntity("rice", "Rice", calories = 130.0, referenceAmount = 100.0, referenceUnit = "g", proteinPerReferenceAmount = 3.0, createdAt = 0, updatedAt = 0)
    private val sauce = rice.copy(id = "sauce", name = "Sauce", proteinPerReferenceAmount = null)
    private val items = listOf(
        RecipeItemEntity("r", "burrito", "rice", "Rice", 0.1, "kg"),
        RecipeItemEntity("s", "burrito", "sauce", "Sauce", 30.0, "g")
    )
    @Test fun fractionalPortionsUseSavedAmountsAndExistingUnitConversion() {
        val result = FlexibleMealCalculator.configure(items, listOf(rice, sauce), mapOf("r" to 1.5))
        assertEquals(195.0, result.calories, 0.001)
        assertEquals(4.5, result.protein.knownGrams, 0.001)
        assertTrue(result.protein.complete)
        assertEquals(0.15, result.ingredients.single().amount, 0.0001)
        assertEquals(1.5, result.ingredients.single().multiplier, 0.0)
    }
    @Test fun ingredientsCanBeRemovedWithoutCountingUnselectedUnknownProtein() {
        val selected = FlexibleMealCalculator.configure(items, listOf(rice, sauce), mapOf("r" to 2.0, "s" to 1.0))
        assertFalse(selected.protein.complete)
        assertEquals(1, selected.protein.missingCount)
        val removed = FlexibleMealCalculator.configure(items, listOf(rice, sauce), mapOf("r" to 0.5, "s" to 0.0))
        assertTrue(removed.protein.complete)
        assertEquals(65.0, removed.calories, 0.001)
        assertTrue(FlexibleMealCalculator.configure(items, listOf(rice), emptyMap()).ingredients.isEmpty())
    }
    @Test fun snapshotDoesNotChangeWhenSourceIsEdited() {
        val result = FlexibleMealCalculator.configure(items, listOf(rice), mapOf("r" to 1.0))
        FlexibleMealCalculator.configure(items, listOf(rice.copy(name = "Changed", calories = 999.0)), mapOf("r" to 2.0))
        assertEquals("Rice", result.ingredients.single().name)
        assertEquals(130.0, result.calories, 0.001)
    }
    @Test fun legacyRecipesDefaultToNormalAndEditorRoundTripsFlexibleFlag() {
        val recipe = RecipeEntity("burrito", "Burrito", createdAt = 1, updatedAt = 1)
        assertFalse(recipe.flexibleMeal)
        assertFalse(RecipeDraft.from(recipe, items).flexibleMeal)
        val draft = RecipeDraft.from(recipe, items).copy(flexibleMeal = true)
        assertTrue(draft.toEntity(recipe, recipe.id, 2).flexibleMeal)
        assertTrue(RecipeDraft.from(draft.toEntity(recipe, recipe.id, 2), items).flexibleMeal)
    }
    @Test fun rejectsInvalidAndNonFiniteMultipliers() {
        listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { value ->
            assertTrue(runCatching { FlexibleMealCalculator.configure(items, listOf(rice), mapOf("r" to value)) }.isFailure)
        }
    }
}
