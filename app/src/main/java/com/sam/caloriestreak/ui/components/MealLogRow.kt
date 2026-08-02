package com.sam.caloriestreak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.sam.caloriestreak.domain.protein.ProteinFormatter
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MealLogRow(
    meal: MealLogEntity,
    onDelete: (MealLogEntity) -> Unit,
    modifier: Modifier = Modifier,
    onRecalculateProtein: ((MealLogEntity) -> Result<Unit>)? = null,
    onSetManualProtein: ((MealLogEntity, Double?) -> Result<Unit>)? = null
) {
    var confirmDelete by remember(meal.id) { mutableStateOf(false) }
    var editProtein by remember(meal.id) { mutableStateOf(false) }
    Card(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppDimensions.Space4)) {
                Text(meal.recipeName, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${meal.portionDescription} · ${meal.calories.toInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    meal.proteinGramsSnapshot?.let { "${ProteinFormatter.grams(it)} protein" } ?: "Protein unknown",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (meal.proteinGramsSnapshot != null) AppColors.Cyan else AppColors.Warning
                )
                Text(
                    Instant.ofEpochMilli(meal.timeMillis)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onRecalculateProtein != null || onSetManualProtein != null) {
                IconButton(onClick = { editProtein = true }) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit protein for ${meal.recipeName}")
                }
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
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }

    if (editProtein) {
        MealProteinCorrectionDialog(
            meal = meal,
            onDismiss = { editProtein = false },
            onRecalculateProtein = onRecalculateProtein,
            onSetManualProtein = onSetManualProtein
        )
    }
}

@Composable
private fun MealProteinCorrectionDialog(
    meal: MealLogEntity,
    onDismiss: () -> Unit,
    onRecalculateProtein: ((MealLogEntity) -> Result<Unit>)?,
    onSetManualProtein: ((MealLogEntity, Double?) -> Result<Unit>)?
) {
    var manualProtein by remember(meal.id) { mutableStateOf(meal.proteinGramsSnapshot?.toString().orEmpty()) }
    var error by remember(meal.id) { mutableStateOf<String?>(null) }
    val parsedManual = manualProtein.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
    val manualValid = manualProtein.isBlank() || (parsedManual != null && parsedManual >= 0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text("Correct historical protein") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)) {
                Text("Stored calorie snapshot: ${meal.calories.toInt()} kcal")
                Text(
                    "Stored protein snapshot: ${meal.proteinGramsSnapshot?.let(ProteinFormatter::grams) ?: "Unknown"}",
                    color = if (meal.proteinGramsSnapshot != null) AppColors.Cyan else AppColors.Warning
                )
                if (meal.recipeId != null) {
                    Text(
                        "Recalculate protein from the recipe as it exists now. This explicitly replaces only the historical protein snapshot; calories remain unchanged.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    OutlinedTextField(
                        value = manualProtein,
                        onValueChange = { manualProtein = it; error = null },
                        label = { Text("Protein") },
                        suffix = { Text("g") },
                        supportingText = { Text("Leave blank to mark protein as unknown") },
                        isError = !manualValid,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            when {
                meal.recipeId != null && onRecalculateProtein != null -> {
                    TextButton(onClick = {
                        onRecalculateProtein(meal).fold(
                            onSuccess = { onDismiss() },
                            onFailure = { error = it.message ?: "Could not recalculate protein" }
                        )
                    }) { Text("Recalculate protein") }
                }
                meal.recipeId == null && onSetManualProtein != null -> {
                    TextButton(
                        enabled = manualValid,
                        onClick = {
                            onSetManualProtein(meal, parsedManual).fold(
                                onSuccess = { onDismiss() },
                                onFailure = { error = it.message ?: "Could not update protein" }
                            )
                        }
                    ) { Text("Save protein") }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
