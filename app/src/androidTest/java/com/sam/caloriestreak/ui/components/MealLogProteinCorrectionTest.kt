package com.sam.caloriestreak.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.ui.theme.CalorieStreakTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MealLogProteinCorrectionTest {
    @get:Rule val composeRule = createComposeRule()

    private fun meal(id: String, recipeId: String?, protein: Double?) = MealLogEntity(
        id = id,
        dateEpochDay = 1,
        timeMillis = 1,
        recipeId = recipeId,
        recipeName = if (recipeId == null) "Restaurant meal" else "Saved recipe",
        portionDescription = "1 serving",
        portionMultiplier = 1.0,
        calories = 850.0,
        proteinGramsSnapshot = protein,
        proteinDataComplete = protein != null,
        missingProteinItemCount = if (protein == null) 1 else 0,
        createdAt = 1,
        updatedAt = 1
    )

    @Test fun manualProteinIsChangedOnlyAfterExplicitSaveAndCaloriesRemainVisible() {
        var callbackCount = 0
        var savedProtein: Double? = null
        val manual = meal("manual", null, null)
        composeRule.setContent {
            CalorieStreakTheme {
                MealLogRow(
                    meal = manual,
                    onDelete = {},
                    onSetManualProtein = { _, value ->
                        callbackCount++
                        savedProtein = value
                        Result.success(Unit)
                    }
                )
            }
        }
        composeRule.runOnIdle { assertEquals(0, callbackCount) }
        composeRule.onNodeWithContentDescription("Edit protein for Restaurant meal").performClick()
        composeRule.onNodeWithText("Stored calorie snapshot: 850 kcal").assertIsDisplayed()
        composeRule.onNodeWithText("Stored protein snapshot: Unknown").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, callbackCount) }
        composeRule.onNode(hasSetTextAction()).performTextInput("35")
        composeRule.onNodeWithText("Save protein").performClick()
        composeRule.runOnIdle {
            assertEquals(1, callbackCount)
            assertEquals(35.0, savedProtein ?: -1.0, 0.0001)
        }
    }

    @Test fun recipeProteinRequiresExplicitRecalculationConfirmation() {
        var callbackCount = 0
        val recipe = meal("recipe-meal", "recipe", 20.0)
        composeRule.setContent {
            CalorieStreakTheme {
                MealLogRow(
                    meal = recipe,
                    onDelete = {},
                    onRecalculateProtein = {
                        callbackCount++
                        Result.success(Unit)
                    }
                )
            }
        }
        composeRule.onNodeWithContentDescription("Edit protein for Saved recipe").performClick()
        composeRule.onNodeWithText("Stored calorie snapshot: 850 kcal").assertIsDisplayed()
        composeRule.onNodeWithText("Stored protein snapshot: 20.0 g").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, callbackCount) }
        composeRule.onNodeWithText("Recalculate protein").performClick()
        composeRule.runOnIdle { assertEquals(1, callbackCount) }
    }
}
