package com.sam.caloriestreak.ui.ingredients

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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.domain.search.SearchMatcher
import com.sam.caloriestreak.ui.components.AppSearchField

@Composable
fun IngredientsScreen(
    ingredients: List<IngredientEntity>,
    onAdd: (String, Double, Double, String) -> Unit,
    onDelete: (IngredientEntity) -> Unit,
    onOpenRecipes: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<IngredientEntity?>(null) }
    val filtered = remember(ingredients, query) {
        ingredients.filter { SearchMatcher.matches(query, it.name, it.brand, it.category) }
            .sortedBy { it.name.lowercase() }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add ingredient")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        label = "Search ingredients",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onOpenRecipes) {
                        Icon(Icons.Outlined.MenuBook, contentDescription = "Open recipes")
                    }
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Text(if (query.isBlank()) "No ingredients yet." else "No ingredients match your search.")
                }
            }
            items(filtered, key = { it.id }) { ingredient ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(ingredient.name)
                            ingredient.brand?.takeIf { it.isNotBlank() }?.let { Text(it) }
                            Text("${ingredient.calories.toInt()} kcal per ${ingredient.referenceAmount} ${ingredient.referenceUnit}")
                        }
                        IconButton(onClick = { pendingDelete = ingredient }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete ${ingredient.name}")
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        IngredientDialog(
            onDismiss = { showDialog = false },
            onSave = { name, calories, amount, unit ->
                onAdd(name, calories, amount, unit)
                showDialog = false
            }
        )
    }

    pendingDelete?.let { ingredient ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ingredient?") },
            text = { Text("Delete ${ingredient.name}? Ingredients used by recipes should be archived instead.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    onDelete(ingredient)
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun IngredientDialog(onDismiss: () -> Unit, onSave: (String, Double, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("100") }
    var unit by remember { mutableStateOf("g") }
    val valid = name.isNotBlank() && (calories.toDoubleOrNull() ?: -1.0) >= 0.0 &&
        (amount.toDoubleOrNull() ?: 0.0) > 0.0 && unit.isNotBlank()
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
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onSave(name.trim(), calories.toDouble(), amount.toDouble(), unit.trim()) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}