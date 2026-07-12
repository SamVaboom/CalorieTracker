package com.sam.caloriestreak.ui.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.domain.calculation.ScoreCalculator
import com.sam.caloriestreak.domain.history.HistoryGraphDefaults
import com.sam.caloriestreak.domain.history.HistoryMetric
import com.sam.caloriestreak.domain.history.HistoryRange
import com.sam.caloriestreak.domain.history.HistorySeriesBuilder
import com.sam.caloriestreak.ui.components.MealLogRow
import java.time.LocalDate

private enum class HistoryMode(val label: String) {
    LIST("List"),
    GRAPH("Graph")
}

@Composable
fun HistoryScreen(
    meals: List<MealLogEntity>,
    dailyLogs: List<DailyLogEntity>,
    targetCalories: Double,
    onDelete: (MealLogEntity) -> Unit
) {
    var mode by rememberSaveable { mutableStateOf(HistoryMode.LIST) }
    var metric by rememberSaveable { mutableStateOf(HistoryGraphDefaults.metric) }
    var range by rememberSaveable { mutableStateOf(HistoryGraphDefaults.range) }

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryMode.entries.forEach { option ->
                FilterChip(
                    selected = mode == option,
                    onClick = { mode = option },
                    label = { Text(option.label) }
                )
            }
        }

        when (mode) {
            HistoryMode.LIST -> HistoryList(meals, dailyLogs, onDelete)
            HistoryMode.GRAPH -> HistoryGraphMode(
                meals = meals,
                dailyLogs = dailyLogs,
                targetCalories = targetCalories,
                metric = metric,
                onMetricChange = { metric = it },
                range = range,
                onRangeChange = { range = it }
            )
        }
    }
}

@Composable
private fun HistoryList(
    meals: List<MealLogEntity>,
    dailyLogs: List<DailyLogEntity>,
    onDelete: (MealLogEntity) -> Unit
) {
    val mealsByDay = remember(meals) { meals.groupBy { it.dateEpochDay } }
    val dailyByDay = remember(dailyLogs) { dailyLogs.associateBy { it.dateEpochDay } }
    val days = remember(meals, dailyLogs) {
        (meals.map { it.dateEpochDay } + dailyLogs.map { it.dateEpochDay }).distinct().sortedDescending()
    }
    val scoreCalculator = remember { ScoreCalculator() }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (days.isEmpty()) {
            item { Text("No meal history yet.") }
        }
        days.forEach { day ->
            val dayMeals = mealsByDay[day].orEmpty()
            val total = dayMeals.sumOf { it.calories }
            val daily = dailyByDay[day]
            val score = daily?.score ?: scoreCalculator.calculate(total)
            item(key = "day-$day") {
                Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(LocalDate.ofEpochDay(day).toString())
                        Text("${total.toInt()} kcal · ${score.toInt()}%")
                        when {
                            daily?.manualCheatDay == true -> Text("Manual freeze day")
                            daily?.freezeUsed == true -> Text("Freeze protected")
                            daily?.finalized == true -> Text(if (daily.streakSuccessful) "Streak safe" else "Streak failed")
                        }
                    }
                }
            }
            if (dayMeals.isEmpty()) {
                item(key = "empty-$day") { Text("No meals logged for this date.") }
            } else {
                items(dayMeals, key = { it.id }) { meal ->
                    MealLogRow(meal = meal, onDelete = onDelete)
                }
            }
        }
    }
}

@Composable
private fun HistoryGraphMode(
    meals: List<MealLogEntity>,
    dailyLogs: List<DailyLogEntity>,
    targetCalories: Double,
    metric: HistoryMetric,
    onMetricChange: (HistoryMetric) -> Unit,
    range: HistoryRange,
    onRangeChange: (HistoryRange) -> Unit
) {
    val today = LocalDate.now().toEpochDay()
    val points = remember(meals, dailyLogs, range, today) {
        HistorySeriesBuilder.build(
            meals = meals,
            dailyLogs = dailyLogs,
            range = range,
            todayEpochDay = today
        )
    }
    val hasData = remember(points, dailyLogs) { HistorySeriesBuilder.hasTrackedData(points, dailyLogs) }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Metric")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryMetric.entries.forEach { option ->
                    FilterChip(
                        selected = metric == option,
                        onClick = { onMetricChange(option) },
                        label = { Text(option.label) }
                    )
                }
            }
        }
        item {
            Text("Time range")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryRange.entries.forEach { option ->
                    FilterChip(
                        selected = range == option,
                        onClick = { onRangeChange(option) },
                        label = { Text(option.label) }
                    )
                }
            }
        }
        item {
            if (!hasData) {
                Text("No stored data exists in this range yet.")
            } else {
                HistoryChart(
                    points = points,
                    metric = metric,
                    targetCalories = targetCalories
                )
            }
        }
    }
}