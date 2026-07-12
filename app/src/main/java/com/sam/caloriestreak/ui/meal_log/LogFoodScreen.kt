package com.sam.caloriestreak.ui.meal_log

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.ui.RecipeSummary

@Composable
fun LogFoodScreen(
    recipes: List<RecipeSummary>,
    onLogRecipe: (RecipeSummary, Double, String) -> Unit,
    onManual: (String, Double) -> Unit
) {
    var manualDialog by remember { mutableStateOf(false) }
    Column(Modifier.padding(16.dp)) {
        Button(onClick = { manualDialog = true }) { Text("Manual calories") }
        LazyColumn {
            items(recipes) { summary ->
                Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(summary.recipe.name)
                        Text("${summary.totalCalories.toInt()} kcal total · ${summary.caloriesPerServing.toInt()} per serving")
                        Row {
                            TextButton(onClick = { onLogRecipe(summary, 1.0 / summary.recipe.servings, "1 serving") }) { Text("1 serving") }
                            TextButton(onClick = { onLogRecipe(summary, 0.5, "Half") }) { Text("½") }
                            TextButton(onClick = { onLogRecipe(summary, 1.0, "Full") }) { Text("Full") }
                        }
                    }
                }
            }
        }
    }
    if (manualDialog) ManualDialog({ manualDialog = false }) { description, calories ->
        onManual(description, calories)
        manualDialog = false
    }
}

@Composable
private fun ManualDialog(onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var description by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual calorie entry") },
        text = {
            Column {
                OutlinedTextField(description, { description = it }, label = { Text("Description") })
                OutlinedTextField(calories, { calories = it }, label = { Text("Calories") })
            }
        },
        confirmButton = { TextButton(onClick = { onSave(description, calories.toDoubleOrNull() ?: -1.0) }) { Text("Log") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
