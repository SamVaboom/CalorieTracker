package com.sam.caloriestreak.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MealLogRow(
    meal: MealLogEntity,
    onDelete: (MealLogEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmDelete by remember(meal.id) { mutableStateOf(false) }
    Card(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(meal.recipeName, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${meal.portionDescription} · ${meal.calories.toInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    Instant.ofEpochMilli(meal.timeMillis)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete ${meal.recipeName}")
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Remove logged food?") },
            text = { Text("Only this ${meal.recipeName} entry will be removed. The recipe itself is not changed.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete(meal)
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}