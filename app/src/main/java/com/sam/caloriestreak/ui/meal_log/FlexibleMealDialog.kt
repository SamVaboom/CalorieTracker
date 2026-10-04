package com.sam.caloriestreak.ui.meal_log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.domain.calculation.FlexibleMealCalculator
import com.sam.caloriestreak.domain.protein.ProteinFormatter
import com.sam.caloriestreak.ui.RecipeSummary
import com.sam.caloriestreak.ui.theme.AppDimensions

@Composable
internal fun FlexibleMealDialog(
    summary: RecipeSummary,
    ingredients: List<IngredientEntity>,
    onDismiss: () -> Unit,
    onSave: (Map<String, Double>) -> Result<Unit>
) {
    var portions by rememberSaveable(summary.recipe.id) { mutableStateOf(hashMapOf<String, Double>()) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val result = remember(summary, ingredients, portions) {
        runCatching { FlexibleMealCalculator.configure(summary.items, ingredients, portions) }
    }
    val configuration = result.getOrNull()
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(summary.recipe.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)) {
                Text("Current total: ${configuration?.calories?.toInt() ?: 0} kcal", style = MaterialTheme.typography.titleMedium)
                configuration?.let { Text(ProteinFormatter.known(it.protein)) }
                Text("Choose ingredients. 1× uses the saved amount; adjust in 0.5 portions.", style = MaterialTheme.typography.bodySmall)
                LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                    items(summary.items, key = { it.id }) { item ->
                        val multiplier = portions[item.id] ?: 0.0
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(AppDimensions.Space8)) {
                                Text(item.ingredientName, style = MaterialTheme.typography.titleSmall)
                                Text("1× = ${item.amount} ${item.unit}", style = MaterialTheme.typography.bodySmall)
                                if (multiplier == 0.0) {
                                    TextButton(onClick = { portions = HashMap(portions).apply { put(item.id, 1.0) }; error = null }, modifier = Modifier.semantics { contentDescription = "Add ${item.ingredientName}" }) { Text("ADD") }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(onClick = { portions = HashMap(portions).apply { put(item.id, (multiplier - 0.5).coerceAtLeast(0.0)) }; error = null }, modifier = Modifier.semantics { contentDescription = "Decrease ${item.ingredientName}" }) { Text("−") }
                                        Text("$multiplier× · ${item.amount * multiplier} ${item.unit}", modifier = Modifier.weight(1f))
                                        TextButton(onClick = { portions = HashMap(portions).apply { put(item.id, multiplier + 0.5) }; error = null }, modifier = Modifier.semantics { contentDescription = "Increase ${item.ingredientName}" }) { Text("+") }
                                    }
                                }
                            }
                        }
                    }
                }
                (error ?: result.exceptionOrNull()?.message)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = !saving && configuration?.ingredients?.isNotEmpty() == true, onClick = {
                saving = true
                onSave(portions.toMap()).onFailure { error = it.message; saving = false }
            }) { Text("Add Meal") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancel") } }
    )
}
