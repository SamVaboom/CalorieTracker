package com.sam.caloriestreak.ui.history

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.ui.theme.CalorieStreakTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.junit.Rule
import org.junit.Test

class HistoryDayExpansionTest {
    @get:Rule val rule = createComposeRule()
    @Test fun dayStartsCollapsedAndTogglesMeals() {
        val day = LocalDate.now().minusDays(1)
        val meal = MealLogEntity("meal", day.toEpochDay(), 0, recipeName = "Test burrito", portionDescription = "Full", portionMultiplier = 1.0, calories = 600.0, createdAt = 0, updatedAt = 0)
        rule.setContent { CalorieStreakTheme { HistoryScreen(listOf(meal), emptyList(), emptyList(), 1650.0, null, {}) } }
        rule.onNodeWithText("Test burrito").assertDoesNotExist()
        val date = day.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))
        rule.onNodeWithText(date).performClick()
        rule.onNodeWithText("Test burrito").assertIsDisplayed()
        rule.onNodeWithText(date).performClick()
        rule.onNodeWithText("Test burrito").assertDoesNotExist()
    }
}
