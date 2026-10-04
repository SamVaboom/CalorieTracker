package com.sam.caloriestreak.ui.meal_log

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.ui.RecipeSummary
import com.sam.caloriestreak.ui.theme.CalorieStreakTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class FlexibleMealFlowTest {
    @get:Rule val rule = createComposeRule()
    private val rice = IngredientEntity("rice", "Rice", calories = 130.0, referenceAmount = 100.0, referenceUnit = "g", proteinPerReferenceAmount = 3.0, createdAt = 1, updatedAt = 1)
    private val summary = RecipeSummary(
        RecipeEntity("burrito", "Burrito", createdAt = 1, updatedAt = 1, flexibleMeal = true),
        listOf(RecipeItemEntity("row", "burrito", "rice", "Rice", 100.0, "g")), 130.0, 130.0
    )
    @Test fun selectionIsRequiredSupportsFractionsRemovalAndFinalLogging() {
        var normalLogs = 0
        var logged: Map<String, Double>? = null
        rule.setContent {
            CalorieStreakTheme {
                LogFoodScreen(listOf(summary), { _, _, _ -> normalLogs++ }, { _, _, _ -> }, listOf(rice), { _, portions -> logged = portions; Result.success(Unit) })
            }
        }
        rule.onNodeWithText("Choose ingredients").performClick()
        rule.runOnIdle { assertEquals(0, normalLogs); assertNull(logged) }
        rule.onNodeWithText("Add Meal").assertIsNotEnabled()
        rule.onNodeWithContentDescription("Add Rice").performClick()
        rule.onNodeWithText("Current total: 130 kcal").assertIsDisplayed()
        rule.onNodeWithContentDescription("Increase Rice").performClick()
        rule.onNodeWithText("Current total: 195 kcal").assertIsDisplayed()
        repeat(3) { rule.onNodeWithContentDescription("Decrease Rice").performClick() }
        rule.onNodeWithContentDescription("Add Rice").assertIsDisplayed()
        rule.onNodeWithText("Add Meal").assertIsNotEnabled()
        rule.onNodeWithContentDescription("Add Rice").performClick()
        rule.onNodeWithContentDescription("Decrease Rice").performClick()
        rule.onNodeWithText("Current total: 65 kcal").assertIsDisplayed()
        rule.onNodeWithText("Add Meal").performClick()
        rule.runOnIdle { assertEquals(mapOf("row" to 0.5), logged); assertEquals(0, normalLogs) }
    }
    @Test fun normalRecipeStillLogsOneServingImmediately() {
        var multiplier: Double? = null
        rule.setContent {
            CalorieStreakTheme {
                LogFoodScreen(listOf(summary.copy(recipe = summary.recipe.copy(flexibleMeal = false, servings = 2.0))), { _, portion, _ -> multiplier = portion }, { _, _, _ -> })
            }
        }
        rule.onNodeWithText("1 serving").performClick()
        rule.runOnIdle { assertEquals(0.5, multiplier!!, 0.0) }
        rule.onNodeWithText("Add Meal").assertDoesNotExist()
    }
}
