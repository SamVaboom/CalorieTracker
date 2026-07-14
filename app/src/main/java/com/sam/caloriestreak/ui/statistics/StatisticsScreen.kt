package com.sam.caloriestreak.ui.statistics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.domain.calculation.ScoreCalculator
import com.sam.caloriestreak.domain.calculation.ScoreDisplay
import com.sam.caloriestreak.domain.weight.WeightStats
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date

private enum class StatisticsSection(val label: String) {
    CALORIES("Calories"), STREAKS("Streaks & Freezes"), WEIGHT("Weight"), ACHIEVEMENTS("Achievements")
}

@Composable
fun StatisticsScreen(
    meals: List<MealLogEntity>,
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
    var section by remember { mutableStateOf(StatisticsSection.CALORIES) }
    val calculator = remember(targetCalories) { ScoreCalculator.forTarget(targetCalories) }
    val byDay = meals.groupBy { it.dateEpochDay }.mapValues { (_, values) -> values.sumOf { it.calories } }
    val today = LocalDate.now().toEpochDay()
    val last7 = (0L..6L).map { byDay[today - it] ?: 0.0 }
    val last30 = (0L..29L).map { byDay[today - it] ?: 0.0 }

    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            StatisticsSection.entries.forEach { option ->
                FilterChip(selected = section == option, onClick = { section = option }, label = { Text(option.label) }, modifier = Modifier.padding(end = 4.dp))
            }
        }
        Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Column(Modifier.padding(16.dp)) {
                when (section) {
                    StatisticsSection.CALORIES -> {
                        Text("7-day average: ${last7.average().toInt()} kcal · ${ScoreDisplay.percent(last7.map(calculator::calculate).average())}%")
                        Text("30-day average: ${last30.average().toInt()} kcal · ${ScoreDisplay.percent(last30.map(calculator::calculate).average())}%")
                        Text("Tracked calorie days: ${byDay.size}")
                        Text("Current target: ${targetCalories.toInt()} kcal")
                    }
                    StatisticsSection.STREAKS -> {
                        Text("Current streak: $currentStreak")
                        Text("Best streak: $bestStreak")
                        Text("Stored freezes: $freezes")
                        Text("Freeze progress: $freezeProgress / $freezeRequiredDays")
                    }
                    StatisticsSection.WEIGHT -> {
                        Text("Latest: ${kg(weight.latest)}")
                        Text("First: ${kg(weight.first)}")
                        Text("Change since first: ${change(weight.changeFromFirst)}")
                        Text("Change since previous: ${change(weight.changeFromPrevious)}")
                        Text("Lowest: ${kg(weight.lowest)}")
                        Text("Highest: ${kg(weight.highest)}")
                        Text("Last-week average: ${kg(weight.averageWeek)}")
                        Text("Last-month average: ${kg(weight.averageMonth)}")
                        Text("Past-year average: ${kg(weight.averageYear)}")
                        Text("All-time average: ${kg(weight.averageAll)}")
                        Text("Entries: ${weight.entryCount} on ${weight.distinctDates} distinct dates")
                        Text("Longest recording gap: ${weight.longestGapDays} days")
                        Text("Most recent: ${weight.mostRecentTimestamp?.let { DateFormat.getDateInstance().format(Date(it)) } ?: "—"}")
                    }
                    StatisticsSection.ACHIEVEMENTS -> Text("Earned: $earnedAchievements of $totalAchievements")
                }
            }
        }
    }
}

private fun kg(value: Double?): String = value?.let { "%.1f kg".format(it) } ?: "—"
private fun change(value: Double?): String = value?.let { "%+.1f kg".format(it) } ?: "—"
