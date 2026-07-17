package com.sam.caloriestreak.ui.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity
import com.sam.caloriestreak.domain.weight.WeightStats
import com.sam.caloriestreak.ui.AppUiState
import com.sam.caloriestreak.ui.achievements.AchievementPopupHost
import com.sam.caloriestreak.ui.achievements.AchievementsScreen
import com.sam.caloriestreak.ui.dashboard.DashboardScreen
import com.sam.caloriestreak.ui.grocery.GroceryScreen
import com.sam.caloriestreak.ui.history.HistoryScreen
import com.sam.caloriestreak.ui.ingredients.IngredientsScreen
import com.sam.caloriestreak.ui.meal_log.LogFoodScreen
import com.sam.caloriestreak.ui.meal_log.ProteinCorrectionScreen
import com.sam.caloriestreak.ui.more.MoreScreen
import com.sam.caloriestreak.ui.recipes.RecipesScreen
import com.sam.caloriestreak.ui.settings.SettingsScreen
import com.sam.caloriestreak.ui.statistics.StatisticsScreen
import com.sam.caloriestreak.ui.theme.CalorieStreakTheme
import com.sam.caloriestreak.ui.weight.WeightScreen

private const val DARK_BACKGROUND = 0xFF0B0E14

@Preview(name = "Dashboard populated", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun DashboardPreview() {
    CalorieStreakTheme {
        DashboardScreen(
            state = AppUiState(
                todayCalories = 1590.0,
                todayScore = 92.0,
                todayEffectiveScore = 92.0,
                target = 1650.0,
                status = "Good",
                currentStreak = 14,
                bestStreak = 31,
                freezes = 2,
                freezeProgress = 4
            ),
            onHistory = {},
            onStatistics = {},
            onFreezeToday = {},
            onDeleteMeal = {}
        )
    }
}

@Preview(name = "Dashboard large text", widthDp = 390, heightDp = 844, fontScale = 1.35f, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun DashboardLargeTextPreview() = DashboardPreview()

@Preview(name = "Log Food empty", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun LogFoodPreview() {
    CalorieStreakTheme { LogFoodScreen(emptyList(), { _, _, _ -> }, { _, _, _ -> }) }
}

@Preview(name = "Recipes empty", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun RecipesPreview() {
    CalorieStreakTheme { RecipesScreen(emptyList(), emptyList(), { _, _ -> }, {}) }
}

@Preview(name = "Ingredients empty", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun IngredientsPreview() {
    CalorieStreakTheme { IngredientsScreen(emptyList(), { _, _ -> }, {}, {}) }
}

@Preview(name = "History empty", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun HistoryPreview() {
    CalorieStreakTheme { HistoryScreen(emptyList(), emptyList(), emptyList(), 1650.0, 80.0, {}) }
}

@Preview(name = "Statistics All", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun StatisticsPreview() {
    CalorieStreakTheme {
        StatisticsScreen(
            meals = emptyList(),
            ingredients = emptyList(),
            currentStreak = 14,
            bestStreak = 31,
            targetCalories = 1650.0,
            freezes = 2,
            freezeProgress = 4,
            freezeRequiredDays = 7,
            weight = WeightStats(latest = 94.2, first = 96.2, changeFromFirst = -2.0, changeFromPrevious = -0.4, lowest = 94.2, highest = 96.2, averageYear = 95.1, averageAll = 95.1),
            earnedAchievements = 12,
            totalAchievements = 115
        )
    }
}

@Preview(name = "Weight empty", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun WeightPreview() {
    CalorieStreakTheme { WeightScreen(emptyList(), WeightStats(), 80.0, { _, _, _ -> Result.success(Unit) }, { _, _, _, _ -> Result.success(Unit) }, {}) }
}

@Preview(name = "Achievements", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun AchievementsPreview() {
    CalorieStreakTheme { AchievementsScreen(emptyList(), {}) }
}

@Preview(name = "Grocery empty", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun GroceryPreview() {
    CalorieStreakTheme { GroceryScreen(emptyList(), emptyList(), emptyList(), { _, _ -> }, {}, {}, {}) }
}

@Preview(name = "Protein corrections empty", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun ProteinCorrectionsPreview() {
    CalorieStreakTheme { ProteinCorrectionScreen(emptyList(), {}, { Result.success(Unit) }, { _, _ -> Result.success(Unit) }) }
}

@Preview(name = "More", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun MorePreview() {
    CalorieStreakTheme { MoreScreen(94.2, 12, 115, 2, {}, {}, {}, {}, {}) }
}

@Preview(name = "Settings", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun SettingsPreview() {
    CalorieStreakTheme { SettingsScreen(1650.0, 80.0, 7) { _, _ -> Result.success(Unit) } }
}

@Preview(name = "Achievement popup", widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = DARK_BACKGROUND)
@Composable
private fun AchievementPopupPreview() {
    CalorieStreakTheme {
        AchievementPopupHost(
            pendingAchievements = listOf(
                EarnedAchievementEntity(
                    id = "preview",
                    achievementId = "protein_triple_digits",
                    earnedAt = System.currentTimeMillis(),
                    triggeringEpochDay = null,
                    progressAtUnlock = 100.0
                )
            ),
            pendingSummary = null,
            onDismissAchievement = {},
            onDismissSummary = {},
            onOpenAchievements = {}
        )
    }
}
