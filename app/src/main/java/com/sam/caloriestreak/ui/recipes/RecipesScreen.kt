package com.sam.caloriestreak.ui.recipes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.ui.RecipeSummary

@Composable
fun RecipesScreen(
    ingredients: List<IngredientEntity>,
    recipes: List<RecipeSummary>,
    onAdd: (String, Double, List<Pair<IngredientEntity, Double>>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(Modifier.padding(16.dp)) {
        Button(onClick = { showDialog = true }, enabled = ingredients.isNotEmpty()) { Text("Add recipe") }
        if (ingredients.isEmpty()) Text("Add ingredients first.")
        LazyColumn {
            items(recipes) { summary ->
                Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(summary.recipe.name)
                        Text("${summary.totalCalories.toInt()} kcal total")
                        Text("${summary.caloriesPerServing.toInt()} kcal per serving")
                        summary.items.forEach { Text("• ${it.amount} ${it.unit} ${it.ingredientName}") }
                    }
                }
            }
        }
    }
    if (showDialog) RecipeDialog(ingredients, { showDialog = false }) { name, servings, selected ->
        onAdd(name, servings, selected)
        showDialog = false
    }
}

@Composable
private fun RecipeDialog(
    ingredients: List<IngredientEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Double, List<Pair<IngredientEntity, Double>>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("2") }
    val selected = remember { mutableStateMapOf<String, Boolean>() }
    val amounts = remember { mutableStateMapOf<String, String>() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New recipe") },
        text = {
            LazyColumn {
                item { OutlinedTextField(name, { name = it }, label = { Text("Recipe name") }) }
                item { OutlinedTextField(servings, { servings = it }, label = { Text("Servings") }) }
                items(ingredients) { ingredient ->
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                        Checkbox(selected[ingredient.id] == true, { selected[ingredient.id] = it })
                        Column(Modifier.weight(1f)) {
                            Text(ingredient.name)
                            OutlinedTextField(
                                amounts[ingredient.id] ?: "",
                                { amounts[ingredient.id] = it },
                                label = { Text("Amount (${ingredient.referenceUnit})") },
                                enabled = selected[ingredient.id] == true
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val chosen = ingredients.filter { selected[it.id] == true }
                    .map { it to (amounts[it.id]?.toDoubleOrNull() ?: 0.0) }
                onSave(name, servings.toDoubleOrNull() ?: 0.0, chosen)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
