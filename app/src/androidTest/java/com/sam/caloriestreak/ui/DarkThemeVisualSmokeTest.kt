package com.sam.caloriestreak.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.sam.caloriestreak.domain.weight.WeightStats
import com.sam.caloriestreak.ui.achievements.AchievementsScreen
import com.sam.caloriestreak.ui.dashboard.DashboardScreen
import com.sam.caloriestreak.ui.history.HistoryScreen
import com.sam.caloriestreak.ui.meal_log.LogFoodScreen
import com.sam.caloriestreak.ui.more.MoreScreen
import com.sam.caloriestreak.ui.recipes.RecipesScreen
import com.sam.caloriestreak.ui.settings.SettingsScreen
import com.sam.caloriestreak.ui.statistics.StatisticsScreen
import com.sam.caloriestreak.ui.theme.CalorieStreakTheme
import com.sam.caloriestreak.ui.weight.WeightScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DarkThemeVisualSmokeTest {
    @get:Rule val composeRule = createComposeRule()

    private fun assertScreenRemainsDark(content: @Composable () -> Unit) {
        composeRule.setContent { CalorieStreakTheme { content() } }
        composeRule.waitForIdle()
        val pixels = composeRule.onRoot().captureToImage().toPixelMap()
        val sampleXs = listOf(0, pixels.width / 4, pixels.width / 2, pixels.width * 3 / 4, pixels.width - 1)
        val sampleYs = listOf(0, pixels.height / 4, pixels.height / 2, pixels.height * 3 / 4, pixels.height - 1)
        val averageLuminance = sampleXs.flatMap { x -> sampleYs.map { y -> pixels[x.coerceIn(0, pixels.width - 1), y.coerceIn(0, pixels.height - 1)].luminance() } }.average()
        assertTrue("Screen became excessively bright: luminance=$averageLuminance", averageLuminance < 0.55)
    }

    @Test fun dashboardDarkScreenshotGuard() = assertScreenRemainsDark {
        DashboardScreen(
            AppUiState(todayCalories = 1590.0, todayScore = 92.0, todayEffectiveScore = 92.0, currentStreak = 14, bestStreak = 31, freezes = 2, freezeProgress = 4),
            {}, {}, {}, {}
        )
    }

    @Test fun logFoodDarkScreenshotGuard() = assertScreenRemainsDark {
        LogFoodScreen(emptyList(), { _, _, _ -> }, { _, _ -> })
    }

    @Test fun recipesDarkScreenshotGuard() = assertScreenRemainsDark {
        RecipesScreen(emptyList(), emptyList(), { _, _ -> }, {})
    }

    @Test fun historyDarkScreenshotGuard() = assertScreenRemainsDark {
        HistoryScreen(emptyList(), emptyList(), emptyList(), 1650.0, 80.0, {})
    }

    @Test fun statisticsDarkScreenshotGuard() = assertScreenRemainsDark {
        StatisticsScreen(emptyList(), 14, 31, 1650.0, 2, 4, 7, WeightStats(), 12, 83)
    }

    @Test fun weightDarkScreenshotGuard() = assertScreenRemainsDark {
        WeightScreen(emptyList(), WeightStats(), 80.0, { _, _, _ -> Result.success(Unit) }, { _, _, _, _ -> Result.success(Unit) }, {})
    }

    @Test fun achievementsDarkScreenshotGuard() = assertScreenRemainsDark {
        AchievementsScreen(emptyList(), {})
    }

    @Test fun moreDarkScreenshotGuard() = assertScreenRemainsDark {
        MoreScreen(94.2, 12, 83, 2, {}, {}, {}, {})
    }

    @Test fun settingsDarkScreenshotGuard() = assertScreenRemainsDark {
        SettingsScreen(1650.0, 80.0, 7) { _, _ -> Result.success(Unit) }
    }
}
