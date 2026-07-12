package com.sam.caloriestreak.ui.recipes

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
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.domain.calculation.IngredientCalorieCalculator
import com.sam.caloriestreak.domain.search.SearchMatcher
import com.sam.caloriestreak.ui.RecipeSummary
import com.sam.caloriestreak.ui.components.AppSearchField

@Composable
fun RecipesScreen(
    ingredients: List<IngredientEntity>,
    recipes: List<RecipeSummary>,
    onAdd: (String, Double, List<Pair<IngredientEntity, Double>>) -> Unit,
    onOpenIngredients: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val filteredRecipes = remember(recipes, query) {
        recipes.filter { SearchMatcher.matches(query, it.recipe.name, it.recipe.description) }
            .sortedBy { it.recipe.name.lowercase() }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier,
                content = { Icon(Icons.Default.Add, contentDescription = "Add recipe") }
            )
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
                        label = "Search recipes",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onOpenIngredients) {
                        Icon(Icons.Outlined.Inventory2, contentDescription = "Open ingredients")
                    }
                }
            }
            if (ingredients.isEmpty()) {
                item { Text("Add ingredients before creating a recipe.") }
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
                        Text("${summary.totalCalories.toInt()} kcal total")
                        Text("${summary.caloriesPerServing.toInt()} kcal per serving")
                        summary.items.forEach { Text("• ${it.amount} ${it.unit} ${it.ingredientName}") }
                    }
                }
            }
        }
    }

    if (showDialog) {
        RecipeDialog(
            ingredients = ingredients,
            onDismiss = { showDialog = false },
            onSave = { name, servings, selected ->
                onAdd(name, servings, selected)
                showDialog = false
            }
        )
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
    var ingredientQuery by remember { mutableStateOf("") }
    val selected = remember { mutableStateMapOf<String, Boolean>() }
    val amounts = remember { mutableStateMapOf<String, String>() }
    val filteredIngredients = remember(ingredients, ingredientQuery) {
        ingredients.filter {
            SearchMatcher.matches(ingredientQuery, it.name, it.brand, it.category)
        }.sortedBy { it.name.lowercase() }
    }
    val selectedWithAmounts = ingredients.filter { selected[it.id] == true }.map { ingredient ->
        ingredient to (amounts[ingredient.id]?.toDoubleOrNull() ?: 0.0)
    }
    val totalCalories = selectedWithAmounts.sumOf { (ingredient, amount) ->
        if (amount <= 0.0) 0.0 else IngredientCalorieCalculator.calories(
            ingredient.calories,
            ingredient.referenceAmount,
            amount
        )
    }
    val servingsValue = servings.toDoubleOrNull() ?: 0.0
    val valid = name.isNotBlank() && servingsValue > 0.0 && selectedWithAmounts.any { it.second > 0.0 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New recipe") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(name, { name = it }, label = { Text("Recipe name") }) }
                item { OutlinedTextField(servings, { servings = it }, label = { Text("Servings") }) }
                item {
                    Text(
                        "${totalCalories.toInt()} kcal total" +
                            if (servingsValue > 0) " · ${(totalCalories / servingsValue).toInt()} per serving" else ""
                    )
                }
                item {
                    AppSearchField(
                        query = ingredientQuery,
                        onQueryChange = { ingredientQuery = it },
                        label = "Search ingredients"
                    )
                }
                if (filteredIngredients.isEmpty()) {
                    item { Text("No ingredients match your search.") }
                }
                items(filteredIngredients, key = { it.id }) { ingredient ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selected[ingredient.id] == true,
                            onCheckedChange = { selected[ingredient.id] = it }
                        )
                        Column(Modifier.weight(1f)) {
                            Text(ingredient.name)
                            OutlinedTextField(
                                value = amounts[ingredient.id] ?: "",
                                onValueChange = { amounts[ingredient.id] = it },
                                label = { Text("Amount (${ingredient.referenceUnit})") },
                                enabled = selected[ingredient.id] == true,
                                singleLine = true
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onSave(name.trim(), servingsValue, selectedWithAmounts) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}