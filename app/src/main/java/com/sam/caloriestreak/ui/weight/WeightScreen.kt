package com.sam.caloriestreak.ui.weight

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import com.sam.caloriestreak.domain.weight.WeightStats
import java.text.DateFormat
import java.util.Date

private enum class WeightMode { LIST, GRAPH }
private enum class WeightRange(val days: Long?) { WEEK(7), MONTH(30), YEAR(365), ALL(null) }

@Composable
fun WeightScreen(
    entries: List<WeightEntryEntity>,
    stats: WeightStats,
    onAdd: (Double, Long, String?) -> Result<Unit>,
    onUpdate: (WeightEntryEntity, Double, Long, String?) -> Result<Unit>,
    onDelete: (WeightEntryEntity) -> Unit
) {
    var mode by remember { mutableStateOf(WeightMode.LIST) }
    var range by remember { mutableStateOf(WeightRange.MONTH) }
    var editor by remember { mutableStateOf<WeightEntryEntity?>(null) }
    var adding by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Weight")
            Text("Latest: ${stats.latest?.let { "%.1f kg".format(it) } ?: "—"}")
            Text("Since first: ${stats.changeFromFirst?.let { "%+.1f kg".format(it) } ?: "—"}")
            Text("Lowest / highest: ${stats.lowest?.let { "%.1f".format(it) } ?: "—"} / ${stats.highest?.let { "%.1f kg".format(it) } ?: "—"}")
            Text("Past-year average: ${stats.averageYear?.let { "%.1f kg".format(it) } ?: "—"}")
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
            item { WeightGraph(entries = filter(entries, range)) }
        } else {
            items(entries.sortedByDescending { it.timestamp }, key = { it.id }) { entry ->
                Card {
                    ListItem(
                        headlineContent = { Text("%.1f kg".format(entry.kilograms)) },
                        supportingContent = { Text(DateFormat.getDateTimeInstance().format(Date(entry.timestamp)) + (entry.note?.let { "\n$it" } ?: "")) },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { editor = entry }) { Text("Edit") }
                                TextButton(onClick = { onDelete(entry) }) { Text("Delete") }
                            }
                        }
                    )
                }
            }
        }
    }
    if (adding) WeightEditor(null, onDismiss = { adding = false }) { kg, time, note -> onAdd(kg, time, note).onSuccess { adding = false } }
    editor?.let { entry -> WeightEditor(entry, onDismiss = { editor = null }) { kg, time, note -> onUpdate(entry, kg, time, note).onSuccess { editor = null } } }
}

private fun filter(entries: List<WeightEntryEntity>, range: WeightRange): List<WeightEntryEntity> {
    val cutoff = range.days?.let { System.currentTimeMillis() - it * 86_400_000L } ?: Long.MIN_VALUE
    return entries.filter { it.timestamp >= cutoff }.sortedBy { it.timestamp }
}

@Composable
private fun WeightGraph(entries: List<WeightEntryEntity>) {
    if (entries.size < 2) {
        Text("Add at least two weight entries to show a graph.")
        return
    }
    val min = entries.minOf { it.kilograms }
    val max = entries.maxOf { it.kilograms }
    val span = (max - min).takeIf { it > 0 } ?: 1.0
    val lineColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(220.dp).padding(12.dp)) {
        val path = Path()
        entries.forEachIndexed { index, entry ->
            val x = size.width * index / (entries.size - 1).coerceAtLeast(1)
            val y = size.height - ((entry.kilograms - min) / span * size.height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(lineColor, radius = 5f, center = Offset(x, y))
        }
        drawPath(path, lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
    }
}

@Composable
private fun WeightEditor(entry: WeightEntryEntity?, onDismiss: () -> Unit, onSave: (Double, Long, String?) -> Result<Unit>) {
    var weight by remember { mutableStateOf(entry?.kilograms?.toString() ?: "") }
    var note by remember { mutableStateOf(entry?.note.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry == null) "Add weight" else "Edit weight") },
        text = {
            Column {
                OutlinedTextField(weight, { weight = it }, label = { Text("Kilograms") })
                OutlinedTextField(note, { note = it }, label = { Text("Note (optional)") })
                error?.let { Text(it) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val kg = weight.toDoubleOrNull()
                if (kg == null) error = "Enter a valid weight" else onSave(kg, entry?.timestamp ?: System.currentTimeMillis(), note).onFailure { error = it.message }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
