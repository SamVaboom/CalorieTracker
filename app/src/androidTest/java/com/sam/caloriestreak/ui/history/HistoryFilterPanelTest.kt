package com.sam.caloriestreak.ui.history

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sam.caloriestreak.domain.history.HistoryMetric
import com.sam.caloriestreak.domain.history.HistoryRange
import com.sam.caloriestreak.ui.theme.CalorieStreakTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HistoryFilterPanelTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun panelStartsCollapsedAndSummaryShowsActiveSelections() {
        composeRule.setContent {
            CalorieStreakTheme {
                HistoryScreen(emptyList(), emptyList(), emptyList(), 1650.0, null, {})
            }
        }
        composeRule.onNodeWithTag("history_filter_summary").assertIsDisplayed()
        composeRule.onNodeWithText("Calories · List · Last week").assertIsDisplayed()
        composeRule.onAllNodesWithTag("history_filter_details").assertCountEquals(0)
    }

    @Test fun expandShowsAllControlsAndCollapseRetainsSelections() {
        composeRule.setContent {
            CalorieStreakTheme {
                HistoryScreen(emptyList(), emptyList(), emptyList(), 1650.0, null, {})
            }
        }
        composeRule.onNodeWithContentDescription("Expand History filters").performClick()
        composeRule.onNodeWithTag("history_filter_details").assertIsDisplayed()
        composeRule.onNodeWithText("Protein").assertIsDisplayed()
        composeRule.onNodeWithText("All time").assertIsDisplayed()
        composeRule.onNodeWithText("Graph").performClick()
        composeRule.onNodeWithText("Score %").assertIsDisplayed()
        composeRule.onNodeWithText("Calories", useUnmergedTree = true).performClick()
        composeRule.onNodeWithText("All time").performClick()
        composeRule.onNodeWithContentDescription("Collapse History filters").performClick()
        composeRule.onNodeWithText("Calories · Graph · Calories · All time").assertIsDisplayed()
        composeRule.onAllNodesWithTag("history_filter_details").assertCountEquals(0)
    }

    @Test fun proteinCategoryUpdatesSummaryAndContentWithoutOverlay() {
        composeRule.setContent {
            CalorieStreakTheme {
                HistoryScreen(emptyList(), emptyList(), emptyList(), 1650.0, null, {})
            }
        }
        composeRule.onNodeWithContentDescription("Expand History filters").performClick()
        composeRule.onNodeWithText("Protein").performClick()
        composeRule.onNodeWithText("Graph").performClick()
        composeRule.onNodeWithContentDescription("Collapse History filters").performClick()
        composeRule.onNodeWithText("Protein · Graph · Last week").assertIsDisplayed()
        composeRule.onNodeWithTag("history_content").assertIsDisplayed()

        val panelBottom = composeRule.onNodeWithTag("history_filter_panel").fetchSemanticsNode().boundsInRoot.bottom
        val contentTop = composeRule.onNodeWithTag("history_content").fetchSemanticsNode().boundsInRoot.top
        assertTrue("History filters must remain above content instead of overlaying it", panelBottom <= contentTop)
    }

    @Test fun expandedStateAndSelectionsSurviveRecomposition() {
        var tick by mutableIntStateOf(0)
        composeRule.setContent {
            CalorieStreakTheme {
                Text("tick $tick")
                HistoryScreen(emptyList(), emptyList(), emptyList(), 1650.0, null, {})
            }
        }
        composeRule.onNodeWithContentDescription("Expand History filters").performClick()
        composeRule.onNodeWithText("Weight").performClick()
        composeRule.runOnIdle { tick++ }
        composeRule.onNodeWithTag("history_filter_details").assertIsDisplayed()
        composeRule.onNodeWithText("Weight · List · Last week").assertIsDisplayed()
    }

    @Test fun directPanelChangingFilterInvokesStateCallbacks() {
        var category = HistoryCategory.CALORIES
        var mode = HistoryMode.GRAPH
        var metric = HistoryMetric.SCORE
        var range = HistoryRange.WEEK
        composeRule.setContent {
            CalorieStreakTheme {
                HistoryFilterPanel(
                    category,
                    mode,
                    metric,
                    range,
                    expanded = true,
                    onExpandedChange = {},
                    onCategoryChange = { category = it },
                    onModeChange = { mode = it },
                    onMetricChange = { metric = it },
                    onRangeChange = { range = it }
                )
            }
        }
        composeRule.onNodeWithText("Protein").performClick()
        composeRule.runOnIdle { assertTrue(category == HistoryCategory.PROTEIN) }
    }
}
