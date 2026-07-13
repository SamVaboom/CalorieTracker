package com.sam.caloriestreak.domain.editing

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeEditorTest {
    private val mozzarella = IngredientEntity(
        id = "mozzarella",
        name = "Mozzarella",
        calories = 280.0,
        referenceAmount = 100.0,
        referenceUnit = "g",
        createdAt = 1L,
        updatedAt = 1L
    )
    private val aubergine = IngredientEntity(
        id = "aubergine",
        name = "Aubergine",
        calories = 25.0,
        referenceAmount = 100.0,
        referenceUnit = "g",
        createdAt = 1L,
        updatedAt = 1L
    )
    private val ingredients = listOf(mozzarella, aubergine)
    private val recipe = RecipeEntity(
        id = "pizza",
        name = "Aubergine Pizza",
        description = "Dinner",
        servings = 2.0,
        createdAt = 100L,
        updatedAt = 200L
    )
    private val items = listOf(
        RecipeItemEntity("item-1", recipe.id, mozzarella.id, mozzarella.name, 300.0, "g", "grated"),
        RecipeItemEntity("item-2", recipe.id, aubergine.id, aubergine.name, 600.0, "g", null)
    )

    @Test
    fun existingRecipeLoadsAndPreservesIdentity() {
        val loaded = RecipeDraft.from(recipe, items)
        assertEquals(recipe.name, loaded.name)
        assertEquals(2, loaded.items.size)

        val updatedDraft = loaded.copy(name = "Updated Pizza", servings = 4.0, favorite = true)
        val updated = updatedDraft.toEntity(existing = recipe, id = "ignored", now = 500L)
        assertEquals(recipe.id, updated.id)
        assertEquals(recipe.createdAt, updated.createdAt)
        assertEquals(500L, updated.updatedAt)
        assertEquals("Updated Pizza", updated.name)
        assertEquals(4.0, updated.servings, 0.0001)
        assertTrue(updated.favorite)
    }

    @Test
    fun ingredientCanBeAddedRemovedAndChangedWithoutDuplicates() {
        val draft = RecipeDraft(
            name = "Pizza",
            servings = 2.0,
            items = listOf(
                RecipeIngredientDraft(mozzarella.id, 250.0, "g", "less cheese"),
                RecipeIngredientDraft(aubergine.id, 0.6, "kg", "sliced")
            )
        )
        assertTrue(draft.isValid(ingredients))
        val savedItems = draft.toItems(recipe.id, ingredients, items) { "new-id" }
        assertEquals(2, savedItems.size)
        assertEquals("item-1", savedItems.first { it.ingredientId == mozzarella.id }.id)
        assertEquals("item-2", savedItems.first { it.ingredientId == aubergine.id }.id)
        assertEquals("sliced", savedItems.first { it.ingredientId == aubergine.id }.note)

        val removed = draft.copy(items = draft.items.filterNot { it.ingredientId == aubergine.id })
        assertEquals(1, removed.toItems(recipe.id, ingredients, items) { "new-id" }.size)
    }

    @Test
    fun recipeTotalUsesCurrentIngredientCaloriesAndCompatibleUnits() {
        val draft = RecipeDraft(
            name = "Pizza",
            servings = 2.0,
            items = listOf(
                RecipeIngredientDraft(mozzarella.id, 300.0, "g"),
                RecipeIngredientDraft(aubergine.id, 0.6, "kg")
            )
        )
        assertEquals(990.0, draft.totalCalories(ingredients), 0.0001)

        val correctedMozzarella = mozzarella.copy(calories = 250.0, updatedAt = 2L)
        assertEquals(900.0, draft.totalCalories(listOf(correctedMozzarella, aubergine)), 0.0001)
    }

    @Test
    fun historicalMealSnapshotDoesNotChangeAfterDefinitionEdit() {
        val historical = MealLogEntity(
            id = "meal-1",
            dateEpochDay = 1L,
            timeMillis = 1000L,
            recipeId = recipe.id,
            recipeName = recipe.name,
            portionDescription = "1 serving",
            portionMultiplier = 0.5,
            calories = 495.0,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val editedDraft = RecipeDraft(
            name = "New Pizza Name",
            servings = 2.0,
            items = listOf(RecipeIngredientDraft(mozzarella.id, 200.0, "g"))
        )
        val newTotal = editedDraft.totalCalories(listOf(mozzarella))

        assertEquals(560.0, newTotal, 0.0001)
        assertEquals("Aubergine Pizza", historical.recipeName)
        assertEquals(495.0, historical.calories, 0.0001)
    }

    @Test
    fun invalidRecipeValuesAreRejected() {
        assertFalse(RecipeDraft(name = "", servings = 2.0).isValid(ingredients))
        assertFalse(RecipeDraft(name = "X", servings = 0.0).isValid(ingredients))
        assertFalse(
            RecipeDraft(
                name = "X",
                servings = 1.0,
                items = listOf(RecipeIngredientDraft(mozzarella.id, 10.0, "ml"))
            ).isValid(ingredients)
        )
        assertFalse(
            RecipeDraft(
                name = "X",
                servings = 1.0,
                items = listOf(
                    RecipeIngredientDraft(mozzarella.id, 10.0, "g"),
                    RecipeIngredientDraft(mozzarella.id, 20.0, "g")
                )
            ).isValid(ingredients)
        )
    }
}
