package com.sam.caloriestreak.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import com.sam.caloriestreak.domain.calculation.ScoreCalculator
import com.sam.caloriestreak.domain.calculation.ScoreDisplay
import com.sam.caloriestreak.domain.history.HistoryGraphDefaults
import com.sam.caloriestreak.domain.history.HistoryMetric
import com.sam.caloriestreak.domain.history.HistoryRange
import com.sam.caloriestreak.domain.history.HistorySeriesBuilder
import com.sam.caloriestreak.ui.components.MealLogRow
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date

private enum class HistoryCategory(val label: String) { CALORIES("Calories"), WEIGHT("Weight") }
private enum class HistoryMode(val label: String) { LIST("List"), GRAPH("Graph") }

@Composable
fun HistoryScreen(
    meals: List<MealLogEntity>,
    dailyLogs: List<DailyLogEntity>,
    weights: List<WeightEntryEntity>,
    targetCalories: Double,
    weightGoal: Double?,
    onDelete: (MealLogEntity) -> Unit
) {
    var category by rememberSaveable { mutableStateOf(HistoryCategory.CALORIES) }
    var mode by rememberSaveable { mutableStateOf(HistoryMode.LIST) }
    var metric by rememberSaveable { mutableStateOf(HistoryGraphDefaults.metric) }
    var range by rememberSaveable { mutableStateOf(HistoryGraphDefaults.range) }

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryCategory.entries.forEach { option -> FilterChip(category == option, { category = option }, label = { Text(option.label) }) }
        }
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryMode.entries.forEach { option -> FilterChip(mode == option, { mode = option }, label = { Text(option.label) }) }
        }
        if (category == HistoryCategory.CALORIES) {
            when (mode) {
                HistoryMode.LIST -> CalorieHistoryList(meals, dailyLogs, targetCalories, onDelete)
                HistoryMode.GRAPH -> CalorieGraphMode(meals, dailyLogs, targetCalories, metric, { metric = it }, range, { range = it })
            }
        } else {
            when (mode) {
                HistoryMode.LIST -> WeightHistoryList(weights)
                HistoryMode.GRAPH -> WeightGraphMode(weights, weightGoal, range, { range = it })
            }
        }
    }
}

@Composable
private fun CalorieHistoryList(meals: List<MealLogEntity>, dailyLogs: List<DailyLogEntity>, targetCalories: Double, onDelete: (MealLogEntity) -> Unit) {
    val mealsByDay = remember(meals) { meals.groupBy { it.dateEpochDay } }
    val dailyByDay = remember(dailyLogs) { dailyLogs.associateBy { it.dateEpochDay } }
    val days = remember(meals, dailyLogs) { (meals.map { it.dateEpochDay } + dailyLogs.map { it.dateEpochDay }).distinct().sortedDescending() }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (days.isEmpty()) item { Text("No meal history yet.") }
        days.forEach { day ->
            val dayMeals = mealsByDay[day].orEmpty()
            val total = dayMeals.sumOf { it.calories }
            val daily = dailyByDay[day]
            val score = daily?.score ?: ScoreCalculator.forTarget(targetCalories).calculate(total)
            item(key = "day-$day") {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(LocalDate.ofEpochDay(day).toString())
                        Text("${total.toInt()} kcal · ${ScoreDisplay.percent(score)}%")
                        when {
                            daily?.manualCheatDay == true -> Text("Manual freeze day")
                            daily?.freezeUsed == true -> Text("Freeze protected")
                            daily?.finalized == true -> Text(if (daily.streakSuccessful) "Streak safe" else "Streak failed")
                        }
                    }
                }
            }
            items(dayMeals, key = { it.id }) { meal -> MealLogRow(meal = meal, onDelete = onDelete) }
        }
    }
}

@Composable
private fun CalorieGraphMode(meals: List<MealLogEntity>, dailyLogs: List<DailyLogEntity>, targetCalories: Double, metric: HistoryMetric, onMetricChange: (HistoryMetric) -> Unit, range: HistoryRange, onRangeChange: (HistoryRange) -> Unit) {
    val today = LocalDate.now().toEpochDay()
    val points = remember(meals, dailyLogs, range, today) { HistorySeriesBuilder.build(meals, dailyLogs, range, today) }
    val hasData = remember(points, dailyLogs) { HistorySeriesBuilder.hasTrackedData(points, dailyLogs) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Metric")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { HistoryMetric.entries.forEach { option -> FilterChip(metric == option, { onMetricChange(option) }, label = { Text(option.label) }) } }
        }
        item { RangeChips(range, onRangeChange) }
        item { if (!hasData) Text("No stored data exists in this range yet.") else HistoryChart(points, metric, targetCalories) }
    }
}

@Composable
private fun WeightHistoryList(weights: List<WeightEntryEntity>) {
    val sorted = weights.sortedByDescending { it.timestamp }
    val ascending = weights.sortedBy { it.timestamp }
    val changeById = ascending.mapIndexed { index, entry -> entry.id to if (index == 0) null else entry.kilograms - ascending[index - 1].kilograms }.toMap()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (sorted.isEmpty()) item { Text("No weight history yet.") }
        items(sorted, key = { it.id }) { entry ->
            val change = changeById[entry.id]
            val headline = buildString {
                append("%.1f kg".format(entry.kilograms))
                change?.let { append(" · ${"%+.1f kg".format(it)}") }
            }
            Card { ListItem(headlineContent = { Text(headline) }, supportingContent = { Text(DateFormat.getDateTimeInstance().format(Date(entry.timestamp)) + (entry.note?.let { "\n$it" } ?: "")) }) }
        }
    }
}

@Composable
private fun WeightGraphMode(weights: List<WeightEntryEntity>, weightGoal: Double?, range: HistoryRange, onRangeChange: (HistoryRange) -> Unit) {
    val days = when (range) {
        HistoryRange.WEEK -> 7L
        HistoryRange.MONTH -> 30L
        HistoryRange.YEAR -> 365L
        HistoryRange.ALL -> null
    }
    val cutoff = days?.let { System.currentTimeMillis() - it * 86_400_000L } ?: Long.MIN_VALUE
    val points = weights.filter { it.timestamp >= cutoff }.sortedBy { it.timestamp }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { RangeChips(range, onRangeChange) }
        item {
            if (points.size < 2) Text("Add at least two weight entries in this range.") else {
                val values = points.map { it.kilograms } + listOfNotNull(weightGoal)
                val min = values.minOrNull() ?: 0.0
                val max = values.maxOrNull() ?: 1.0
                val span = (max - min).takeIf { it > 0 } ?: 1.0
                val color = MaterialTheme.colorScheme.primary
                val goalColor = MaterialTheme.colorScheme.tertiary
                Canvas(Modifier.fillMaxWidth().height(240.dp).padding(12.dp)) {
                    val path = Path()
                    points.forEachIndexed { index, entry ->
                        val x = size.width * index / (points.size - 1)
                        val y = size.height - ((entry.kilograms - min) / span * size.height).toFloat()
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(color, 5f, Offset(x, y))
                    }
                    drawPath(path, color, style = Stroke(4f))
                    weightGoal?.let { goal ->
                        val y = size.height - ((goal - min) / span * size.height).toFloat()
                        drawLine(goalColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 3f)
                    }
                }
                Text("Weight in kilograms; only recorded entries are plotted.")
                weightGoal?.let { Text("Goal line: %.1f kg".format(it), color = goalColor) }
            }
        }
    }
}

@Composable
private fun RangeChips(range: HistoryRange, onRangeChange: (HistoryRange) -> Unit) {
    Text("Time range")
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HistoryRange.entries.forEach { option -> FilterChip(range == option, { onRangeChange(option) }, label = { Text(option.label) }) }
    }
}
