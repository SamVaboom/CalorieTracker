package com.sam.caloriestreak.ui.statistics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.domain.calculation.ScoreCalculator
import com.sam.caloriestreak.domain.calculation.ScoreDisplay
import com.sam.caloriestreak.domain.protein.ProteinFormatter
import com.sam.caloriestreak.domain.protein.ProteinRangeStatistics
import com.sam.caloriestreak.domain.protein.ProteinStatisticsCalculator
import com.sam.caloriestreak.domain.weight.WeightStats
import com.sam.caloriestreak.ui.components.AppCard
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.components.AppStatCard
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date

private enum class StatisticsSection(val label: String) {
    ALL("All"),
    CALORIES("Calories"),
    STREAKS("Streaks & Freezes"),
    WEIGHT("Weight"),
    PROTEIN("Protein"),
    ACHIEVEMENTS("Achievements")
}

@Composable
fun StatisticsScreen(
    meals: List<MealLogEntity>,
    ingredients: List<IngredientEntity>,
    currentStreak: Int,
    bestStreak: Int,
    targetCalories: Double,
    freezes: Int,
    freezeProgress: Int,
    freezeRequiredDays: Int,
    weight: WeightStats,
    earnedAchievements: Int,
    totalAchievements: Int
) {
    var section by remember { mutableStateOf(StatisticsSection.ALL) }
    val calculator = remember(targetCalories) { ScoreCalculator.forTarget(targetCalories) }
    val byDay = remember(meals) { meals.groupBy { it.dateEpochDay }.mapValues { (_, values) -> values.sumOf { it.calories } } }
    val today = LocalDate.now().toEpochDay()
    val last7 = (0L..6L).map { byDay[today - it] ?: 0.0 }
    val last30 = (0L..29L).map { byDay[today - it] ?: 0.0 }
    val average7Score = ScoreDisplay.percent(last7.map(calculator::calculate).average())
    val average30Score = ScoreDisplay.percent(last30.map(calculator::calculate).average())
    val completion = if (totalAchievements > 0) earnedAchievements * 100 / totalAchievements else 0
    val protein = remember(meals, ingredients, today) {
        ProteinStatisticsCalculator.calculate(meals, ingredients, today)
    }

    LazyColumn(
        contentPadding = PaddingValues(AppDimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Space16)
    ) {
        item { AppSectionHeader("Statistics", subtitle = "A clear view of your tracking history") }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                StatisticsSection.entries.forEach { option ->
                    FilterChip(
                        selected = section == option,
                        onClick = { section = option },
                        label = { Text(option.label) },
                        modifier = Modifier.padding(end = AppDimensions.Space8)
                    )
                }
            }
        }

        if (section == StatisticsSection.ALL || section == StatisticsSection.CALORIES) {
            item { AppSectionHeader("Calories", subtitle = "Averages use the existing app calculation") }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                    AppStatCard("7-day score", "$average7Score%", Modifier.weight(1f), "${last7.average().toInt()} kcal/day", AppColors.Cyan)
                    AppStatCard("30-day score", "$average30Score%", Modifier.weight(1f), "${last30.average().toInt()} kcal/day", AppColors.Violet)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                    AppStatCard("Tracked days", byDay.size.toString(), Modifier.weight(1f), accent = AppColors.Coral)
                    AppStatCard("Current target", "${targetCalories.toInt()} kcal", Modifier.weight(1f), accent = AppColors.Warning)
                }
            }
        }

        if (section == StatisticsSection.ALL || section == StatisticsSection.STREAKS) {
            item { AppSectionHeader("Streaks & Freezes") }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                    AppStatCard("Current streak", "$currentStreak days", Modifier.weight(1f), accent = AppColors.Coral)
                    AppStatCard("Best streak", "$bestStreak days", Modifier.weight(1f), accent = AppColors.Achievement)
                }
            }
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Text("Freeze inventory", style = MaterialTheme.typography.titleMedium)
                    Text("$freezes / 3 stored", style = MaterialTheme.typography.headlineSmall, color = AppColors.Freeze, modifier = Modifier.padding(top = AppDimensions.Space8))
                    Text(
                        "$freezeProgress / $freezeRequiredDays qualifying days toward the next freeze",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = AppDimensions.Space4)
                    )
                }
            }
        }

        if (section == StatisticsSection.ALL || section == StatisticsSection.WEIGHT) {
            item { AppSectionHeader("Weight", subtitle = "Only recorded entries are included") }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                    AppStatCard("Latest", kg(weight.latest), Modifier.weight(1f), change(weight.changeFromPrevious) + " since previous", AppColors.Weight)
                    AppStatCard("Since first", change(weight.changeFromFirst), Modifier.weight(1f), "First: ${kg(weight.first)}", AppColors.Success)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                    AppStatCard("Lowest", kg(weight.lowest), Modifier.weight(1f), accent = AppColors.Cyan)
                    AppStatCard("Highest", kg(weight.highest), Modifier.weight(1f), accent = AppColors.Coral)
                }
            }
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Text("Weight averages", style = MaterialTheme.typography.titleMedium)
                    StatLine("Last week", kg(weight.averageWeek))
                    StatLine("Last month", kg(weight.averageMonth))
                    StatLine("Past year", kg(weight.averageYear))
                    StatLine("All time", kg(weight.averageAll))
                    StatLine("Entries", "${weight.entryCount} on ${weight.distinctDates} dates")
                    StatLine("Longest gap", "${weight.longestGapDays} days")
                    StatLine("Most recent", weight.mostRecentTimestamp?.let { DateFormat.getDateInstance().format(Date(it)) } ?: "—")
                }
            }
        }

        if (section == StatisticsSection.ALL || section == StatisticsSection.PROTEIN) {
            item {
                AppSectionHeader(
                    "Protein",
                    subtitle = if (protein.historicalDataIncomplete) "Known protein only; some historical entries are incomplete" else "Informational only — no target or success state"
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                    AppStatCard(
                        "Today",
                        if (protein.today.hasKnownData) ProteinFormatter.grams(protein.today.knownGrams) else "Unknown",
                        Modifier.weight(1f),
                        if (protein.today.complete) "Complete" else "Known amount only",
                        AppColors.Cyan
                    )
                    AppStatCard(
                        "Highest day",
                        protein.highestKnownDayGrams?.let(ProteinFormatter::grams) ?: "—",
                        Modifier.weight(1f),
                        "Highest meal: ${protein.highestKnownMealGrams?.let(ProteinFormatter::grams) ?: "—"}",
                        AppColors.Cyan
                    )
                }
            }
            item { ProteinRangeCard("Last 7 days", protein.last7Days) }
            item { ProteinRangeCard("Last 30 days", protein.last30Days) }
            item { ProteinRangeCard("Past year", protein.lastYear) }
            item { ProteinRangeCard("All time", protein.allTime) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                    AppStatCard("100 g days", protein.daysAtLeast100Grams.toString(), Modifier.weight(1f), accent = AppColors.Cyan)
                    AppStatCard(
                        "Ingredient coverage",
                        "${protein.activeIngredientsAssignedPercent.toInt()}%",
                        Modifier.weight(1f),
                        "${protein.activeIngredientsAssigned} assigned · ${protein.activeIngredientsMissing} missing",
                        AppColors.Warning
                    )
                }
            }
        }

        if (section == StatisticsSection.ALL || section == StatisticsSection.ACHIEVEMENTS) {
            item { AppSectionHeader("Achievements") }
            item {
                AppStatCard(
                    title = "Collection progress",
                    value = "$earnedAchievements / $totalAchievements",
                    supportingText = "$completion% complete",
                    accent = AppColors.Achievement,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ProteinRangeCard(title: String, range: ProteinRangeStatistics) {
    AppCard(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        StatLine("Known protein", ProteinFormatter.lifetime(range.knownTotalGrams))
        StatLine("Recorded days", range.recordedDayCount.toString())
        StatLine("Known average / recorded day", ProteinFormatter.grams(range.knownAveragePerRecordedDay))
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = AppDimensions.Space8),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun kg(value: Double?): String = value?.let { "%.1f kg".format(it) } ?: "—"
private fun change(value: Double?): String = value?.let { "%+.1f kg".format(it) } ?: "—"
