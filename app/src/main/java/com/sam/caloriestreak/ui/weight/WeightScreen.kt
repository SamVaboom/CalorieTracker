package com.sam.caloriestreak.ui.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import com.sam.caloriestreak.domain.weight.WeightStats
import com.sam.caloriestreak.ui.components.AppCard
import com.sam.caloriestreak.ui.components.AppChartContainer
import com.sam.caloriestreak.ui.components.AppEmptyState
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.abs

private enum class WeightMode { LIST, GRAPH }
private enum class WeightRange(val days: Long?) { WEEK(7), MONTH(30), YEAR(365), ALL(null) }

@Composable
fun WeightScreen(
    entries: List<WeightEntryEntity>,
    stats: WeightStats,
    weightGoal: Double?,
    onAdd: (Double, Long, String?) -> Result<Unit>,
    onUpdate: (WeightEntryEntity, Double, Long, String?) -> Result<Unit>,
    onDelete: (WeightEntryEntity) -> Unit
) {
    var mode by remember { mutableStateOf(WeightMode.LIST) }
    var range by remember { mutableStateOf(WeightRange.MONTH) }
    var editor by remember { mutableStateOf<WeightEntryEntity?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<WeightEntryEntity?>(null) }
    val ascending = remember(entries) { entries.sortedBy { it.timestamp } }
    val changeById = remember(ascending) {
        ascending.mapIndexed { index, entry -> entry.id to if (index == 0) null else entry.kilograms - ascending[index - 1].kilograms }.toMap()
    }

    LazyColumn(
        contentPadding = PaddingValues(AppDimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Space16)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AppSectionHeader("Weight", subtitle = "Track entries whenever they are useful")
                Button(onClick = { adding = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text("Add", modifier = Modifier.padding(start = AppDimensions.Space4))
                }
            }
        }
        item { WeightHero(stats, weightGoal) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                FilterChip(selected = mode == WeightMode.LIST, onClick = { mode = WeightMode.LIST }, label = { Text("List") })
                FilterChip(selected = mode == WeightMode.GRAPH, onClick = { mode = WeightMode.GRAPH }, label = { Text("Graph") })
            }
        }

        if (mode == WeightMode.GRAPH) {
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                    WeightRange.entries.forEach { option ->
                        FilterChip(
                            selected = range == option,
                            onClick = { range = option },
                            label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
            item { WeightGraph(entries = filter(entries, range), weightGoal = weightGoal) }
        } else if (entries.isEmpty()) {
            item {
                AppEmptyState(
                    icon = Icons.Outlined.MonitorWeight,
                    title = "No weight entries yet",
                    message = "Add your first entry to begin the graph."
                )
            }
        } else {
            item { AppSectionHeader("History", subtitle = "Newest entries appear first") }
            items(entries.sortedByDescending { it.timestamp }, key = { it.id }) { entry ->
                val change = changeById[entry.id]
                val headline = buildString {
                    append("%.1f kg".format(entry.kilograms))
                    change?.let { append(" · ${"%+.1f kg".format(it)}") }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(headline, style = MaterialTheme.typography.titleMedium) },
                        supportingContent = {
                            Text(
                                DateFormat.getDateTimeInstance().format(Date(entry.timestamp)) + (entry.note?.let { "\n$it" } ?: ""),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { editor = entry }) { Icon(Icons.Outlined.Edit, contentDescription = "Edit weight entry") }
                                IconButton(onClick = { deleteCandidate = entry }) { Icon(Icons.Outlined.Delete, contentDescription = "Delete weight entry") }
                            }
                        }
                    )
                }
            }
        }
    }

    if (adding) WeightEditor(null, onDismiss = { adding = false }) { kg, time, note -> onAdd(kg, time, note).onSuccess { adding = false } }
    editor?.let { entry -> WeightEditor(entry, onDismiss = { editor = null }) { kg, time, note -> onUpdate(entry, kg, time, note).onSuccess { editor = null } } }
    deleteCandidate?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Delete weight entry?") },
            text = { Text("This removes the %.1f kg entry from your history.".format(entry.kilograms)) },
            confirmButton = { TextButton(onClick = { deleteCandidate = null; onDelete(entry) }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteCandidate = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun WeightHero(stats: WeightStats, weightGoal: Double?) {
    val first = stats.first
    val latest = stats.latest
    AppCard(Modifier.fillMaxWidth(), emphasized = true) {
        Text("Latest weight", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(latest?.let { "%.1f kg".format(it) } ?: "—", style = MaterialTheme.typography.displayMedium, color = AppColors.Weight)
        Text(
            stats.changeFromPrevious?.let { "%+.1f kg since previous".format(it) } ?: "Add another entry to see the latest change",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            stats.changeFromFirst?.let { "%+.1f kg since first entry".format(it) } ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppDimensions.Space4)
        )

        if (first != null && latest != null && weightGoal != null && abs(first - weightGoal) > 0.001) {
            val total = abs(first - weightGoal)
            val completed = if (weightGoal < first) first - latest else latest - first
            val progress = (completed / total).coerceIn(0.0, 1.0).toFloat()
            val remaining = abs(latest - weightGoal)
            Text("Goal progress", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = AppDimensions.Space20))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(top = AppDimensions.Space8),
                color = AppColors.Weight,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
            Row(Modifier.fillMaxWidth().padding(top = AppDimensions.Space8), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Start %.1f kg".format(first), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.0f%% · %.1f kg remaining".format(progress * 100, remaining), style = MaterialTheme.typography.labelMedium)
                Text("Goal %.1f kg".format(weightGoal), style = MaterialTheme.typography.labelMedium, color = AppColors.Weight)
            }
        } else if (weightGoal != null) {
            Text("Goal: %.1f kg".format(weightGoal), color = AppColors.Weight, modifier = Modifier.padding(top = AppDimensions.Space12))
        }
    }
}

private fun filter(entries: List<WeightEntryEntity>, range: WeightRange): List<WeightEntryEntity> {
    val cutoff = range.days?.let { System.currentTimeMillis() - it * 86_400_000L } ?: Long.MIN_VALUE
    return entries.filter { it.timestamp >= cutoff }.sortedBy { it.timestamp }
}

@Composable
private fun WeightGraph(entries: List<WeightEntryEntity>, weightGoal: Double?) {
    AppChartContainer(
        title = "Weight trend",
        subtitle = "Only actual recorded entries are plotted",
        accent = AppColors.Weight
    ) {
        if (entries.size < 2) {
            Text("Add at least two weight entries in this range.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@AppChartContainer
        }
        var selected by remember(entries) { mutableStateOf<WeightEntryEntity?>(null) }
        val values = entries.map { it.kilograms } + listOfNotNull(weightGoal)
        val min = values.minOrNull() ?: 0.0
        val max = values.maxOrNull() ?: 1.0
        val span = (max - min).takeIf { it > 0 } ?: 1.0
        val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .semantics { contentDescription = "Weight graph with ${entries.size} recorded points${weightGoal?.let { ", goal %.1f kilograms".format(it) } ?: ""}" }
                .pointerInput(entries) {
                    detectTapGestures { tap ->
                        val nearestIndex = entries.indices.minByOrNull { index -> abs(tap.x - size.width * index / (entries.size - 1).coerceAtLeast(1)) }
                        selected = nearestIndex?.let(entries::get)
                    }
                }
        ) {
            repeat(4) { row ->
                val y = size.height * row / 3f
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            val path = Path()
            entries.forEachIndexed { index, entry ->
                val x = size.width * index / (entries.size - 1).coerceAtLeast(1)
                val y = size.height - ((entry.kilograms - min) / span * size.height).toFloat()
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                drawCircle(AppColors.Weight, radius = if (selected?.id == entry.id) 9.dp.toPx() else 5.dp.toPx(), center = Offset(x, y))
            }
            drawPath(path, AppColors.Weight, style = Stroke(width = 3.dp.toPx()))
            weightGoal?.let { goal ->
                val y = size.height - ((goal - min) / span * size.height).toFloat()
                drawLine(AppColors.Achievement, Offset(0f, y), Offset(size.width, y), strokeWidth = 2.dp.toPx())
            }
        }
        selected?.let {
            Text(
                "${DateFormat.getDateTimeInstance().format(Date(it.timestamp))}: %.1f kg".format(it.kilograms),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Weight,
                modifier = Modifier.padding(top = AppDimensions.Space8)
            )
        }
        weightGoal?.let {
            Text("Goal line: %.1f kg".format(it), style = MaterialTheme.typography.labelMedium, color = AppColors.Achievement)
        }
    }
}

@Composable
private fun WeightEditor(entry: WeightEntryEntity?, onDismiss: () -> Unit, onSave: (Double, Long, String?) -> Result<Unit>) {
    val zone = ZoneId.systemDefault()
    val initial = entry?.timestamp?.let { Instant.ofEpochMilli(it).atZone(zone) } ?: Instant.now().atZone(zone)
    var weight by remember { mutableStateOf(entry?.kilograms?.toString() ?: "") }
    var date by remember { mutableStateOf(initial.toLocalDate().toString()) }
    var time by remember { mutableStateOf(initial.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var note by remember { mutableStateOf(entry?.note.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(if (entry == null) "Add weight" else "Edit weight") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)) {
                OutlinedTextField(weight, { weight = it }, label = { Text("Weight") }, suffix = { Text("kg") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(date, { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(time, { time = it }, label = { Text("Time (HH:mm)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(note, { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = !saving, onClick = {
                val kg = weight.toDoubleOrNull()
                val timestamp = runCatching {
                    LocalDate.parse(date).atTime(LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))).atZone(zone).toInstant().toEpochMilli()
                }.getOrNull()
                when {
                    kg == null -> error = "Enter a valid weight"
                    timestamp == null -> error = "Enter a valid date and time"
                    else -> {
                        saving = true
                        onSave(kg, timestamp, note).onFailure { saving = false; error = it.message }
                    }
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancel") } }
    )
}
