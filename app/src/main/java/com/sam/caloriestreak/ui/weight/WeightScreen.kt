package com.sam.caloriestreak.ui.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import com.sam.caloriestreak.domain.weight.WeightStats
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
    val ascending = entries.sortedBy { it.timestamp }
    val changeById = ascending.mapIndexed { index, entry ->
        entry.id to if (index == 0) null else entry.kilograms - ascending[index - 1].kilograms
    }.toMap()

    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Weight")
            Text("Latest: ${stats.latest?.let { "%.1f kg".format(it) } ?: "—"}")
            Text("Previous: ${stats.previous?.let { "%.1f kg".format(it) } ?: "—"}")
            Text("Since previous: ${stats.changeFromPrevious?.let { "%+.1f kg".format(it) } ?: "—"}")
            Text("First: ${stats.first?.let { "%.1f kg".format(it) } ?: "—"}")
            Text("Since first: ${stats.changeFromFirst?.let { "%+.1f kg".format(it) } ?: "—"}")
            Text("Lowest / highest: ${stats.lowest?.let { "%.1f".format(it) } ?: "—"} / ${stats.highest?.let { "%.1f kg".format(it) } ?: "—"}")
            Text("Average: ${stats.averageAll?.let { "%.1f kg".format(it) } ?: "—"}")
            weightGoal?.let { Text("Goal: %.1f kg".format(it)) }
            Button(onClick = { adding = true }) { Text("Add weight") }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = mode == WeightMode.LIST, onClick = { mode = WeightMode.LIST }, label = { Text("List") })
                FilterChip(selected = mode == WeightMode.GRAPH, onClick = { mode = WeightMode.GRAPH }, label = { Text("Graph") })
            }
        }
        if (mode == WeightMode.GRAPH) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WeightRange.entries.forEach { option ->
                        FilterChip(selected = range == option, onClick = { range = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
            }
            item { WeightGraph(entries = filter(entries, range), weightGoal = weightGoal) }
        } else {
            items(entries.sortedByDescending { it.timestamp }, key = { it.id }) { entry ->
                val change = changeById[entry.id]
                val headline = buildString {
                    append("%.1f kg".format(entry.kilograms))
                    change?.let { append(" · ${"%+.1f kg".format(it)}") }
                }
                Card {
                    ListItem(
                        headlineContent = { Text(headline) },
                        supportingContent = { Text(DateFormat.getDateTimeInstance().format(Date(entry.timestamp)) + (entry.note?.let { "\n$it" } ?: "")) },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { editor = entry }) { Text("Edit") }
                                TextButton(onClick = { deleteCandidate = entry }) { Text("Delete") }
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
            title = { Text("Delete weight entry?") },
            text = { Text("This removes the %.1f kg entry from your history.".format(entry.kilograms)) },
            confirmButton = { TextButton(onClick = { deleteCandidate = null; onDelete(entry) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteCandidate = null }) { Text("Cancel") } }
        )
    }
}

private fun filter(entries: List<WeightEntryEntity>, range: WeightRange): List<WeightEntryEntity> {
    val cutoff = range.days?.let { System.currentTimeMillis() - it * 86_400_000L } ?: Long.MIN_VALUE
    return entries.filter { it.timestamp >= cutoff }.sortedBy { it.timestamp }
}

@Composable
private fun WeightGraph(entries: List<WeightEntryEntity>, weightGoal: Double?) {
    if (entries.size < 2) {
        Text("Add at least two weight entries to show a graph.")
        return
    }
    var selected by remember(entries) { mutableStateOf<WeightEntryEntity?>(null) }
    val values = entries.map { it.kilograms } + listOfNotNull(weightGoal)
    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 1.0
    val span = (max - min).takeIf { it > 0 } ?: 1.0
    val lineColor = MaterialTheme.colorScheme.primary
    val goalColor = MaterialTheme.colorScheme.tertiary
    Canvas(
        Modifier.fillMaxWidth().height(220.dp).padding(12.dp).pointerInput(entries) {
            detectTapGestures { tap ->
                val nearestIndex = entries.indices.minByOrNull { index ->
                    abs(tap.x - size.width * index / (entries.size - 1).coerceAtLeast(1))
                }
                selected = nearestIndex?.let(entries::get)
            }
        }
    ) {
        val path = Path()
        entries.forEachIndexed { index, entry ->
            val x = size.width * index / (entries.size - 1).coerceAtLeast(1)
            val y = size.height - ((entry.kilograms - min) / span * size.height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(lineColor, radius = if (selected?.id == entry.id) 9f else 5f, center = Offset(x, y))
        }
        drawPath(path, lineColor, style = Stroke(width = 4f))
        weightGoal?.let { goal ->
            val y = size.height - ((goal - min) / span * size.height).toFloat()
            drawLine(goalColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 3f)
        }
    }
    selected?.let { Text("${DateFormat.getDateTimeInstance().format(Date(it.timestamp))}: %.1f kg".format(it.kilograms)) }
    weightGoal?.let { Text("Goal line: %.1f kg".format(it), color = MaterialTheme.colorScheme.tertiary) }
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
        title = { Text(if (entry == null) "Add weight" else "Edit weight") },
        text = {
            Column {
                OutlinedTextField(weight, { weight = it }, label = { Text("Kilograms") })
                OutlinedTextField(date, { date = it }, label = { Text("Date (YYYY-MM-DD)") })
                OutlinedTextField(time, { time = it }, label = { Text("Time (HH:mm)") })
                OutlinedTextField(note, { note = it }, label = { Text("Note (optional)") })
                error?.let { Text(it) }
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
