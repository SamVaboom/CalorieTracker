package com.sam.caloriestreak.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.sam.caloriestreak.domain.protein.DailyProteinCalculator
import com.sam.caloriestreak.domain.protein.ProteinFormatter
import com.sam.caloriestreak.domain.protein.ProteinHistorySeriesBuilder
import com.sam.caloriestreak.ui.components.AppCard
import com.sam.caloriestreak.ui.components.AppChartContainer
import com.sam.caloriestreak.ui.components.AppEmptyState
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.components.MealLogRow
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.abs

internal enum class HistoryCategory(val label: String) {
    CALORIES("Calories"), WEIGHT("Weight"), PROTEIN("Protein")
}

internal enum class HistoryMode(val label: String) { LIST("List"), GRAPH("Graph") }

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
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = AppDimensions.ScreenPadding, vertical = AppDimensions.Space16)) {
            AppSectionHeader("History", subtitle = "Review calories, weight and known protein over time")
            HistoryFilterPanel(
                category = category,
                mode = mode,
                metric = metric,
                range = range,
                expanded = filtersExpanded,
                onExpandedChange = { filtersExpanded = it },
                onCategoryChange = { category = it },
                onModeChange = { mode = it },
                onMetricChange = { metric = it },
                onRangeChange = { range = it }
            )
        }

        when (category) {
            HistoryCategory.CALORIES -> when (mode) {
                HistoryMode.LIST -> CalorieHistoryList(meals, dailyLogs, targetCalories, range, onDelete)
                HistoryMode.GRAPH -> CalorieGraphMode(meals, dailyLogs, targetCalories, metric, range)
            }
            HistoryCategory.WEIGHT -> when (mode) {
                HistoryMode.LIST -> WeightHistoryList(weights, range)
                HistoryMode.GRAPH -> WeightGraphMode(weights, weightGoal, range)
            }
            HistoryCategory.PROTEIN -> when (mode) {
                HistoryMode.LIST -> ProteinHistoryList(meals, range)
                HistoryMode.GRAPH -> ProteinGraphMode(meals, range)
            }
        }
    }
}

@Composable
internal fun HistoryFilterPanel(
    category: HistoryCategory,
    mode: HistoryMode,
    metric: HistoryMetric,
    range: HistoryRange,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategoryChange: (HistoryCategory) -> Unit,
    onModeChange: (HistoryMode) -> Unit,
    onMetricChange: (HistoryMetric) -> Unit,
    onRangeChange: (HistoryRange) -> Unit
) {
    val summary = buildList {
        add(category.label)
        add(mode.label)
        if (category == HistoryCategory.CALORIES && mode == HistoryMode.GRAPH) add(metric.label)
        add(range.label)
    }.joinToString(" · ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppDimensions.Space12)
            .animateContentSize()
            .testTag("history_filter_panel"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = AppDimensions.Space16, top = AppDimensions.Space8, bottom = AppDimensions.Space8),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.weight(1f).padding(vertical = AppDimensions.Space8)) {
                Icon(Icons.Outlined.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    summary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = AppDimensions.Space8).testTag("history_filter_summary")
                )
            }
            IconButton(onClick = { onExpandedChange(!expanded) }) {
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse History filters" else "Expand History filters"
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.testTag("history_filter_details")
        ) {
            Column(Modifier.fillMaxWidth().padding(start = AppDimensions.Space16, end = AppDimensions.Space16, bottom = AppDimensions.Space16)) {
                FilterLabel("Category")
                ScrollableChips {
                    HistoryCategory.entries.forEach { option ->
                        FilterChip(selected = category == option, onClick = { onCategoryChange(option) }, label = { Text(option.label) })
                    }
                }
                FilterLabel("View")
                ScrollableChips {
                    HistoryMode.entries.forEach { option ->
                        FilterChip(selected = mode == option, onClick = { onModeChange(option) }, label = { Text(option.label) })
                    }
                }
                if (category == HistoryCategory.CALORIES && mode == HistoryMode.GRAPH) {
                    FilterLabel("Metric")
                    ScrollableChips {
                        HistoryMetric.entries.forEach { option ->
                            FilterChip(selected = metric == option, onClick = { onMetricChange(option) }, label = { Text(option.label) })
                        }
                    }
                }
                FilterLabel("Time range")
                ScrollableChips {
                    HistoryRange.entries.forEach { option ->
                        FilterChip(selected = range == option, onClick = { onRangeChange(option) }, label = { Text(option.label) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = AppDimensions.Space8)
    )
}

@Composable
private fun ScrollableChips(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = AppDimensions.Space4),
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)
    ) { content() }
}

@Composable
private fun CalorieHistoryList(
    meals: List<MealLogEntity>,
    dailyLogs: List<DailyLogEntity>,
    targetCalories: Double,
    range: HistoryRange,
    onDelete: (MealLogEntity) -> Unit
) {
    val today = LocalDate.now().toEpochDay()
    val cutoff = range.dayCount?.let { today - it + 1 }
    val mealsByDay = remember(meals) { meals.groupBy { it.dateEpochDay } }
    val dailyByDay = remember(dailyLogs) { dailyLogs.associateBy { it.dateEpochDay } }
    val days = remember(meals, dailyLogs, range, today) {
        (meals.map { it.dateEpochDay } + dailyLogs.map { it.dateEpochDay })
            .distinct()
            .filter { cutoff == null || it >= cutoff }
            .sortedDescending()
    }
    LazyColumn(
        modifier = Modifier.testTag("history_content"),
        contentPadding = PaddingValues(start = AppDimensions.ScreenPadding, end = AppDimensions.ScreenPadding, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)
    ) {
        if (days.isEmpty()) item { AppEmptyState(Icons.Outlined.History, "No calorie history in this range", "Log food or choose another range.") }
        days.forEach { day ->
            val dayMeals = mealsByDay[day].orEmpty()
            val total = dayMeals.sumOf { it.calories }
            val daily = dailyByDay[day]
            val score = daily?.score ?: ScoreCalculator.forTarget(targetCalories).calculate(total)
            item(key = "day-$day") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = BorderStroke(1.dp, scoreColor(score).copy(alpha = 0.35f))
                ) {
                    Column(Modifier.padding(AppDimensions.Space16)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")), style = MaterialTheme.typography.titleMedium)
                            Text("${ScoreDisplay.percent(score)}%", style = MaterialTheme.typography.titleMedium, color = scoreColor(score))
                        }
                        Text("${total.toInt()} kcal · ${dayMeals.size} entries", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = AppDimensions.Space4))
                        when {
                            daily?.manualCheatDay == true -> Text("Manual freeze day", color = AppColors.Freeze, modifier = Modifier.padding(top = AppDimensions.Space4))
                            daily?.freezeUsed == true -> Text("Freeze protected", color = AppColors.Freeze, modifier = Modifier.padding(top = AppDimensions.Space4))
                            daily?.finalized == true -> Text(if (daily.streakSuccessful) "Streak safe" else "Streak failed", color = if (daily.streakSuccessful) AppColors.Success else AppColors.Error, modifier = Modifier.padding(top = AppDimensions.Space4))
                        }
                    }
                }
            }
            items(dayMeals, key = { it.id }) { meal -> MealLogRow(meal = meal, onDelete = onDelete) }
        }
    }
}

@Composable
private fun CalorieGraphMode(
    meals: List<MealLogEntity>,
    dailyLogs: List<DailyLogEntity>,
    targetCalories: Double,
    metric: HistoryMetric,
    range: HistoryRange
) {
    val today = LocalDate.now().toEpochDay()
    val points = remember(meals, dailyLogs, range, today) { HistorySeriesBuilder.build(meals, dailyLogs, range, today) }
    val hasData = remember(points, dailyLogs) { HistorySeriesBuilder.hasTrackedData(points, dailyLogs) }
    LazyColumn(
        modifier = Modifier.testTag("history_content"),
        contentPadding = PaddingValues(start = AppDimensions.ScreenPadding, end = AppDimensions.ScreenPadding, bottom = 96.dp)
    ) {
        item {
            if (!hasData) AppEmptyState(Icons.Outlined.History, "No data in this range", "Choose another range or log food first.")
            else AppChartContainer(
                title = if (metric == HistoryMetric.SCORE) "Daily score" else "Daily calories",
                subtitle = range.label,
                accent = if (metric == HistoryMetric.SCORE) AppColors.Violet else AppColors.Coral
            ) { HistoryChart(points, metric, targetCalories) }
        }
    }
}

@Composable
private fun WeightHistoryList(weights: List<WeightEntryEntity>, range: HistoryRange) {
    val today = LocalDate.now().toEpochDay()
    val cutoff = range.dayCount?.let { today - it + 1 }
    val zone = ZoneId.systemDefault()
    val filtered = remember(weights, range, today) {
        weights.filter {
            cutoff == null || Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate().toEpochDay() >= cutoff
        }.sortedByDescending { it.timestamp }
    }
    val ascending = weights.sortedBy { it.timestamp }
    val changeById = remember(ascending) { ascending.mapIndexed { index, entry -> entry.id to if (index == 0) null else entry.kilograms - ascending[index - 1].kilograms }.toMap() }
    LazyColumn(
        modifier = Modifier.testTag("history_content"),
        contentPadding = PaddingValues(start = AppDimensions.ScreenPadding, end = AppDimensions.ScreenPadding, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)
    ) {
        if (filtered.isEmpty()) item { AppEmptyState(Icons.Outlined.MonitorWeight, "No weight history in this range", "Add a weight entry or choose another range.") }
        items(filtered, key = { it.id }) { entry ->
            val change = changeById[entry.id]
            val headline = buildString {
                append("%.1f kg".format(entry.kilograms))
                change?.let { append(" · ${"%+.1f kg".format(it)}") }
            }
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, AppColors.Weight.copy(alpha = 0.28f))
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text(headline, style = MaterialTheme.typography.titleMedium, color = AppColors.Weight) },
                    supportingContent = { Text(DateFormat.getDateTimeInstance().format(Date(entry.timestamp)) + (entry.note?.let { "\n$it" } ?: "")) }
                )
            }
        }
    }
}

@Composable
private fun WeightGraphMode(weights: List<WeightEntryEntity>, weightGoal: Double?, range: HistoryRange) {
    val cutoff = range.dayCount?.let { System.currentTimeMillis() - it * 86_400_000L } ?: Long.MIN_VALUE
    val points = weights.filter { it.timestamp >= cutoff }.sortedBy { it.timestamp }
    LazyColumn(
        modifier = Modifier.testTag("history_content"),
        contentPadding = PaddingValues(start = AppDimensions.ScreenPadding, end = AppDimensions.ScreenPadding, bottom = 96.dp)
    ) {
        item {
            AppChartContainer(title = "Weight trend", subtitle = "${range.label} · actual entries only", accent = AppColors.Weight) {
                if (points.size < 2) Text("Add at least two weight entries in this range.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else WeightHistoryChart(points, weightGoal)
            }
        }
    }
}

@Composable
private fun ProteinHistoryList(meals: List<MealLogEntity>, range: HistoryRange) {
    val today = LocalDate.now().toEpochDay()
    val cutoff = range.dayCount?.let { today - it + 1 }
    val days = remember(meals, range, today) {
        meals.groupBy { it.dateEpochDay }
            .filterKeys { cutoff == null || it >= cutoff }
            .toList()
            .sortedByDescending { it.first }
    }
    LazyColumn(
        modifier = Modifier.testTag("history_content"),
        contentPadding = PaddingValues(start = AppDimensions.ScreenPadding, end = AppDimensions.ScreenPadding, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)
    ) {
        if (days.isEmpty()) item { AppEmptyState(Icons.Outlined.FitnessCenter, "No protein history in this range", "New meals with protein snapshots will appear here.") }
        days.forEach { (day, dayMeals) ->
            val summary = DailyProteinCalculator.calculate(dayMeals)
            item(key = "protein-day-$day") {
                AppCard(Modifier.fillMaxWidth()) {
                    Text(LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")), style = MaterialTheme.typography.titleMedium)
                    Text(
                        ProteinFormatter.known(summary),
                        color = if (summary.complete) AppColors.Cyan else AppColors.Warning,
                        modifier = Modifier.padding(top = AppDimensions.Space4)
                    )
                    if (!summary.complete) {
                        Text(
                            "${summary.missingCount} logged item${if (summary.missingCount == 1) " has" else "s have"} unknown protein",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(dayMeals, key = { "protein-${it.id}" }) { meal ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), shape = MaterialTheme.shapes.medium) {
                    Row(Modifier.fillMaxWidth().padding(AppDimensions.Space12), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(meal.recipeName, modifier = Modifier.weight(1f))
                        Text(
                            meal.proteinGramsSnapshot?.let(ProteinFormatter::grams) ?: "Protein unknown",
                            color = if (meal.proteinGramsSnapshot != null) AppColors.Cyan else AppColors.Warning
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProteinGraphMode(meals: List<MealLogEntity>, range: HistoryRange) {
    val today = LocalDate.now().toEpochDay()
    val points = remember(meals, range, today) { ProteinHistorySeriesBuilder.build(meals, range, today) }
    LazyColumn(
        modifier = Modifier.testTag("history_content"),
        contentPadding = PaddingValues(start = AppDimensions.ScreenPadding, end = AppDimensions.ScreenPadding, bottom = 96.dp)
    ) {
        item {
            if (points.isEmpty()) AppEmptyState(Icons.Outlined.FitnessCenter, "No known protein in this range", "Unknown-only dates are not plotted as zero.")
            else AppChartContainer(title = "Known daily protein", subtitle = range.label, accent = AppColors.Cyan) {
                ProteinHistoryChart(points)
            }
        }
    }
}

@Composable
private fun WeightHistoryChart(points: List<WeightEntryEntity>, weightGoal: Double?) {
    var selectedIndex by remember { mutableIntStateOf(points.lastIndex) }
    val values = points.map { it.kilograms } + listOfNotNull(weightGoal)
    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 1.0
    val span = (max - min).takeIf { it > 0 } ?: 1.0
    val grid = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    points.getOrNull(selectedIndex)?.let {
        Text("${DateFormat.getDateTimeInstance().format(Date(it.timestamp))} · %.1f kg".format(it.kilograms), color = AppColors.Weight, modifier = Modifier.padding(bottom = AppDimensions.Space8))
    }
    Canvas(
        Modifier.fillMaxWidth().height(240.dp).semantics { contentDescription = "Weight history graph with ${points.size} recorded entries" }.pointerInput(points) {
            detectTapGestures { tap ->
                selectedIndex = points.indices.minByOrNull { index -> abs(tap.x - size.width * index / (points.size - 1).coerceAtLeast(1)) } ?: selectedIndex
            }
        }
    ) {
        repeat(4) { row ->
            val y = size.height * row / 3f
            drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        val path = Path()
        points.forEachIndexed { index, entry ->
            val x = size.width * index / (points.size - 1)
            val y = size.height - ((entry.kilograms - min) / span * size.height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(AppColors.Weight, if (index == selectedIndex) 8.dp.toPx() else 5.dp.toPx(), Offset(x, y))
        }
        drawPath(path, AppColors.Weight, style = Stroke(3.dp.toPx()))
        weightGoal?.let { goal ->
            val y = size.height - ((goal - min) / span * size.height).toFloat()
            drawLine(AppColors.Achievement, Offset(0f, y), Offset(size.width, y), strokeWidth = 2.dp.toPx())
        }
    }
    weightGoal?.let { Text("Goal line: %.1f kg".format(it), color = AppColors.Achievement, style = MaterialTheme.typography.labelMedium) }
}

private fun scoreColor(score: Double) = when {
    score < 40.0 -> AppColors.Error
    score < 80.0 -> AppColors.Warning
    score < 85.0 -> AppColors.Cyan
    score < 95.0 -> AppColors.Violet
    else -> AppColors.Success
}
