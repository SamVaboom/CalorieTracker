package com.sam.caloriestreak.ui.recipes

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import com.sam.caloriestreak.domain.editing.RecipeDraft
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.ui.RecipeIngredientSummary
import com.sam.caloriestreak.ui.RecipeSummary
import com.sam.caloriestreak.ui.theme.CalorieStreakTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RecipesExpansionTest {
    @Test fun deletionRequiresConfirmationAndCanBeCancelled() {
        var deleted = 0
        composeRule.setContent { CalorieStreakTheme { RecipesScreen(listOf(cheese, sauce), listOf(pizza()), { _, _ -> }, {}, { deleted++ }) } }
        composeRule.onNodeWithContentDescription("Edit Aubergine Pizza").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.runOnIdle { assertEquals(0, deleted) }
        composeRule.onNodeWithText("Delete this recipe?").assertIsDisplayed()
        // The confirmation dialog is the only active semantics window.
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.runOnIdle { assertEquals(0, deleted) }
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.runOnIdle { assertEquals(1, deleted) }
    }

    @Test fun editorCanEnableFlexibleMeal() {
        var saved: RecipeDraft? = null
        composeRule.setContent { CalorieStreakTheme { RecipesScreen(listOf(cheese, sauce), listOf(pizza()), { _, draft -> saved = draft }, {}) } }
        composeRule.onNodeWithContentDescription("Edit Aubergine Pizza").performClick()
        composeRule.onNodeWithTag("recipe_editor_content").performScrollToNode(hasText("Flexible Meal"))
        composeRule.onNodeWithContentDescription("Flexible Meal").performClick()
        composeRule.onNodeWithText("Save").performClick()
        composeRule.runOnIdle { assertEquals(true, saved?.flexibleMeal) }
    }

    @get:Rule val composeRule = createComposeRule()

    private val cheese = IngredientEntity(
        id = "cheese",
        name = "Mozzarella",
        calories = 280.0,
        referenceAmount = 100.0,
        referenceUnit = "g",
        proteinPerReferenceAmount = 22.0,
        createdAt = 1,
        updatedAt = 1
    )
    private val sauce = IngredientEntity(
        id = "sauce",
        name = "Tomato sauce",
        calories = 45.0,
        referenceAmount = 100.0,
        referenceUnit = "g",
        proteinPerReferenceAmount = null,
        createdAt = 1,
        updatedAt = 1
    )

    private fun pizza() = RecipeSummary(
        recipe = RecipeEntity("pizza", "Aubergine Pizza", "Dinner", 2.0, createdAt = 1, updatedAt = 1),
        items = listOf(
            RecipeItemEntity("cheese-row", "pizza", "cheese", "Mozzarella", 300.0, "g"),
            RecipeItemEntity("sauce-row", "pizza", "sauce", "Tomato sauce", 200.0, "g")
        ),
        totalCalories = 930.0,
        caloriesPerServing = 465.0,
        ingredientDetails = listOf(
            RecipeIngredientSummary(RecipeItemEntity("cheese-row", "pizza", "cheese", "Mozzarella", 300.0, "g"), 840.0, 66.0, true),
            RecipeIngredientSummary(RecipeItemEntity("sauce-row", "pizza", "sauce", "Tomato sauce", 200.0, "g"), 90.0, null, false)
        ),
        knownProteinGrams = 66.0,
        proteinDataComplete = false,
        missingProteinItemCount = 1
    )

    private fun soup() = RecipeSummary(
        recipe = RecipeEntity("soup", "Tomato Soup", servings = 1.0, createdAt = 1, updatedAt = 1),
        items = emptyList(),
        totalCalories = 200.0,
        caloriesPerServing = 200.0
    )

    @Test fun cardStartsCollapsedTapExpandsAndSecondTapCollapses() {
        composeRule.setContent {
            CalorieStreakTheme { RecipesScreen(listOf(cheese, sauce), listOf(pizza()), { _, _ -> }, {}) }
        }
        composeRule.onAllNodesWithText("Mozzarella").assertCountEquals(0)
        composeRule.onNodeWithText("Aubergine Pizza").performClick()
        composeRule.onNodeWithText("Mozzarella").assertIsDisplayed()
        composeRule.onNodeWithText("840 kcal · 66.0 g protein").assertIsDisplayed()
        composeRule.onNodeWithText("90 kcal · Protein not assigned").assertIsDisplayed()
        composeRule.onNodeWithText("Protein data incomplete: 1 ingredient missing").assertIsDisplayed()
        composeRule.onNodeWithText("Aubergine Pizza").performClick()
        composeRule.onAllNodesWithText("Mozzarella").assertCountEquals(0)
    }

    @Test fun editActionDoesNotToggleExpansion() {
        var editCount = 0
        composeRule.setContent {
            CalorieStreakTheme {
                RecipesScreen(listOf(cheese, sauce), listOf(pizza()), { existing, _ -> if (existing != null) editCount++ }, {})
            }
        }
        composeRule.onNodeWithContentDescription("Edit Aubergine Pizza").performClick()
        composeRule.onAllNodesWithText("Mozzarella").assertCountEquals(0)
        composeRule.runOnIdle { assertEquals(0, editCount) }
        composeRule.onNodeWithText("Edit Recipe").assertIsDisplayed()
    }

    @Test fun expansionStateStaysAttachedToStableRecipeIdAcrossSearch() {
        composeRule.setContent {
            CalorieStreakTheme {
                RecipesScreen(listOf(cheese, sauce), listOf(pizza(), soup()), { _, _ -> }, {})
            }
        }
        composeRule.onNodeWithContentDescription("Expand Aubergine Pizza").performClick()
        composeRule.onNodeWithText("Mozzarella").assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).performTextInput("Soup")
        composeRule.onAllNodesWithText("Aubergine Pizza").assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Clear search").performClick()
        composeRule.onNodeWithText("Mozzarella").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Collapse Aubergine Pizza").assertIsDisplayed()
    }
}
