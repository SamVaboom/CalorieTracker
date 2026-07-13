package com.sam.caloriestreak.domain.editing

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.domain.search.SearchMatcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditingSearchRegressionTest {
    @Test
    fun ingredientRenameUsesNewSearchValue() {
        val existing = IngredientEntity(
            id = "1",
            name = "Old Cheese",
            calories = 200.0,
            referenceAmount = 100.0,
            referenceUnit = "g",
            createdAt = 1L,
            updatedAt = 1L
        )
        val updated = IngredientDraft.from(existing).copy(name = "Mozzarella").toEntity(existing, "ignored", 2L)

        assertTrue(SearchMatcher.matches("mozza", updated.name, updated.brand, updated.category))
        assertFalse(SearchMatcher.matches("old cheese", updated.name, updated.brand, updated.category))
    }

    @Test
    fun recipeRenameUsesNewSearchValue() {
        val existing = RecipeEntity(
            id = "1",
            name = "Old Pizza",
            servings = 2.0,
            createdAt = 1L,
            updatedAt = 1L
        )
        val updated = RecipeDraft(name = "Aubergine Pizza", servings = 2.0)
            .toEntity(existing, "ignored", 2L)

        assertTrue(SearchMatcher.matches("aubergine", updated.name, updated.description))
        assertFalse(SearchMatcher.matches("old pizza", updated.name, updated.description))
    }
}
