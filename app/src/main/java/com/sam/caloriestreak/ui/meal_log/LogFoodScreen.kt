package com.sam.caloriestreak.ui.meal_log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.domain.search.SearchMatcher
import com.sam.caloriestreak.ui.RecipeSummary
import com.sam.caloriestreak.ui.components.AppSearchField

@Composable
fun LogFoodScreen(
    recipes: List<RecipeSummary>,
    onLogRecipe: (RecipeSummary, Double, String) -> Unit,
    onManual: (String, Double) -> Unit
) {
    var manualDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val filteredRecipes = remember(recipes, query) {
        recipes.filter { summary ->
            SearchMatcher.matches(
                query,
                summary.recipe.name,
                summary.recipe.description,
                *summary.items.map { it.ingredientName }.toTypedArray()
            )
        }.sortedBy { it.recipe.name.lowercase() }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { manualDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add manual calories")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                AppSearchField(
                    query = query,
                    onQueryChange = { query = it },
                    label = "Search recipes"
                )
            }
            if (filteredRecipes.isEmpty()) {
                item {
                    Text(if (query.isBlank()) "No recipes yet." else "No recipes match your search.")
                }
            }
            items(filteredRecipes, key = { it.recipe.id }) { summary ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(summary.recipe.name)
                        Text("${summary.totalCalories.toInt()} kcal total · ${summary.caloriesPerServing.toInt()} per serving")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { onLogRecipe(summary, 1.0 / summary.recipe.servings, "1 serving") }) {
                                Text("1 serving")
                            }
                            TextButton(onClick = { onLogRecipe(summary, 0.5, "Half") }) { Text("½") }
                            TextButton(onClick = { onLogRecipe(summary, 1.0, "Full") }) { Text("Full") }
                        }
                    }
                }
            }
        }
    }

    if (manualDialog) {
        ManualDialog(
            onDismiss = { manualDialog = false },
            onSave = { description, calories ->
                onManual(description, calories)
                manualDialog = false
            }
        )
    }
}

@Composable
private fun ManualDialog(onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var description by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    val valid = description.isNotBlank() && (calories.toDoubleOrNull() ?: -1.0) >= 0.0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual calorie entry") },
        text = {
            Column {
                OutlinedTextField(description, { description = it }, label = { Text("Description") })
                OutlinedTextField(calories, { calories = it }, label = { Text("Calories") })
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onSave(description.trim(), calories.toDouble()) }
            ) { Text("Log") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}