package com.sam.caloriestreak.ui.ingredients

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.ui.theme.CalorieStreakTheme
import org.junit.Rule
import org.junit.Test

class IngredientsProteinFilterTest {
    @get:Rule val composeRule = createComposeRule()

    private val missingCheese = IngredientEntity(
        id = "cheese",
        name = "Cheese",
        calories = 280.0,
        referenceAmount = 100.0,
        referenceUnit = "g",
        proteinPerReferenceAmount = null,
        createdAt = 1,
        updatedAt = 1
    )
    private val assignedTomato = IngredientEntity(
        id = "tomato",
        name = "Tomato",
        calories = 20.0,
        referenceAmount = 100.0,
        referenceUnit = "g",
        proteinPerReferenceAmount = 0.0,
        createdAt = 1,
        updatedAt = 1
    )

    @Test fun missingAndAssignedFiltersWorkTogetherWithSearch() {
        composeRule.setContent {
            CalorieStreakTheme {
                IngredientsScreen(listOf(missingCheese, assignedTomato), { _, _ -> }, {}, {})
            }
        }
        composeRule.onNodeWithText("Protein not assigned").assertIsDisplayed()
        composeRule.onNodeWithText("0.0 g protein / 100.0 g").assertIsDisplayed()

        composeRule.onNodeWithText("Protein missing").performClick()
        composeRule.onNodeWithText("Cheese").assertIsDisplayed()
        composeRule.onAllNodesWithText("Tomato").assertCountEquals(0)

        composeRule.onNode(hasSetTextAction()).performTextInput("tomato")
        composeRule.onNodeWithText("No matching ingredients").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Clear search").performClick()
        composeRule.onNodeWithText("Protein assigned").performClick()
        composeRule.onNodeWithText("Tomato").assertIsDisplayed()
        composeRule.onAllNodesWithText("Cheese").assertCountEquals(0)
    }

    @Test fun ingredientFormShowsProteinAndNoLongerShowsBrand() {
        composeRule.setContent {
            CalorieStreakTheme { IngredientsScreen(emptyList(), { _, _ -> }, {}, {}) }
        }
        composeRule.onNodeWithContentDescription("Add ingredient").performClick()
        composeRule.onNodeWithText("Protein").assertIsDisplayed()
        composeRule.onNodeWithText("Reference quantity").assertIsDisplayed()
        composeRule.onAllNodesWithText("Brand").assertCountEquals(0)
    }
}
