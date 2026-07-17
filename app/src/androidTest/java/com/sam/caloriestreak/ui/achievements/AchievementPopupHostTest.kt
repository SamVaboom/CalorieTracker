package com.sam.caloriestreak.ui.achievements

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import com.sam.caloriestreak.data.local.entity.AchievementPopupSummaryEntity
import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity
import com.sam.caloriestreak.ui.theme.CalorieStreakTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AchievementPopupHostTest {
    @get:Rule val composeRule = createComposeRule()

    private fun record(id: String, achievementId: String = "bullseye", earnedAt: Long = 1L) = EarnedAchievementEntity(
        id = id,
        achievementId = achievementId,
        earnedAt = earnedAt,
        triggeringEpochDay = 1,
        progressAtUnlock = 1.0
    )

    @Test fun popupAppearsImmediatelyAndDismissalAdvancesQueue() {
        var pending by mutableStateOf(listOf(record("first"), record("second", "three_in_a_row", 2)))
        composeRule.setContent {
            CalorieStreakTheme {
                AchievementPopupHost(
                    pendingAchievements = pending,
                    pendingSummary = null,
                    onDismissAchievement = { id -> pending = pending.filterNot { it.id == id } },
                    onDismissSummary = {},
                    onOpenAchievements = {}
                )
            }
        }

        composeRule.onNodeWithText("Bullseye").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Close achievement popup").performClick()
        composeRule.onNodeWithText("Three in a Row").assertIsDisplayed()
    }

    @Test fun recompositionDoesNotDuplicatePopup() {
        var tick by mutableIntStateOf(0)
        composeRule.setContent {
            CalorieStreakTheme {
                Text("$tick")
                AchievementPopupHost(listOf(record("one")), null, {}, {}, {})
            }
        }
        composeRule.runOnIdle { tick++ }
        composeRule.onAllNodesWithTag("achievement_popup_card").assertCountEquals(1)
    }

    @Test fun androidBackDismissesPopup() {
        var pending by mutableStateOf(listOf(record("one")))
        composeRule.setContent {
            CalorieStreakTheme {
                AchievementPopupHost(
                    pending,
                    null,
                    { pending = emptyList() },
                    {},
                    {}
                )
            }
        }
        pressBack()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("achievement_popup_card").assertCountEquals(0)
    }

    @Test fun outsideTapDoesNotDismissPopup() {
        var dismissed = false
        composeRule.setContent {
            CalorieStreakTheme {
                AchievementPopupHost(listOf(record("one")), null, { dismissed = true }, {}, {})
            }
        }
        composeRule.onNodeWithTag("achievement_popup_scrim").performTouchInput { click(Offset(4f, 4f)) }
        composeRule.onNodeWithTag("achievement_popup_card").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(false, dismissed) }
    }

    @Test fun retroactiveBulkUnlockShowsOneSummaryInsteadOfIndividuals() {
        var summary by mutableStateOf<AchievementPopupSummaryEntity?>(AchievementPopupSummaryEntity("summary", 12, 1L))
        composeRule.setContent {
            CalorieStreakTheme {
                AchievementPopupHost(
                    pendingAchievements = listOf(record("individual")),
                    pendingSummary = summary,
                    onDismissAchievement = {},
                    onDismissSummary = { summary = null },
                    onOpenAchievements = {}
                )
            }
        }
        composeRule.onNodeWithText("12 achievements unlocked from your existing history.").assertIsDisplayed()
        composeRule.onAllNodesWithText("Bullseye").assertCountEquals(0)
    }
}
