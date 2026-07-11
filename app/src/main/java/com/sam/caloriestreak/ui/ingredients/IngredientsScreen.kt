package com.sam.caloriestreak.ui.ingredients

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
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
import com.sam.caloriestreak.data.local.entity.IngredientEntity

@Composable
fun IngredientsScreen(
    ingredients: List<IngredientEntity>,
    onAdd: (String, Double, Double, String) -> Unit,
    onDelete: (IngredientEntity) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(Modifier.padding(16.dp)) {
        Button(onClick = { showDialog = true }) { Text("Add ingredient") }
        LazyColumn {
            items(ingredients) { ingredient ->
                Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Row(Modifier.padding(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(ingredient.name)
                            Text("${ingredient.calories.toInt()} kcal per ${ingredient.referenceAmount} ${ingredient.referenceUnit}")
                        }
                        IconButton(onClick = { onDelete(ingredient) }) { Text("×") }
                    }
                }
            }
        }
    }
    if (showDialog) IngredientDialog(
        onDismiss = { showDialog = false },
        onSave = { name, calories, amount, unit ->
            onAdd(name, calories, amount, unit)
            showDialog = false
        }
    )
}

@Composable
private fun IngredientDialog(onDismiss: () -> Unit, onSave: (String, Double, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("100") }
    var unit by remember { mutableStateOf("g") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New ingredient") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Name") })
                OutlinedTextField(calories, { calories = it }, label = { Text("Calories") })
                OutlinedTextField(amount, { amount = it }, label = { Text("Reference amount") })
                OutlinedTextField(unit, { unit = it }, label = { Text("Unit") })
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, calories.toDoubleOrNull() ?: -1.0, amount.toDoubleOrNull() ?: 0.0, unit) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
