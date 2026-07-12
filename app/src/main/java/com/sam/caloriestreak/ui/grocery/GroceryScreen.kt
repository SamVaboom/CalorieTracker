package com.sam.caloriestreak.ui.grocery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import com.sam.caloriestreak.data.local.entity.GroceryItemEntity
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.domain.search.SearchMatcher
import com.sam.caloriestreak.ui.RecipeSummary
import com.sam.caloriestreak.ui.components.AppSearchField

@Composable
fun GroceryScreen(
    recipes: List<RecipeSummary>,
    ingredients: List<IngredientEntity>,
    items: List<GroceryItemEntity>,
    onGenerate: (RecipeSummary, Double) -> Unit,
    onAddIngredient: (IngredientEntity) -> Unit,
    onToggle: (GroceryItemEntity) -> Unit,
    onClear: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filteredRecipes = remember(recipes, query) {
        recipes.filter { SearchMatcher.matches(query, it.recipe.name) }
            .sortedBy { it.recipe.name.lowercase() }
    }
    val filteredIngredients = remember(ingredients, query) {
        ingredients.filter { SearchMatcher.matches(query, it.name, it.brand, it.category) }
            .sortedBy { it.name.lowercase() }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AppSearchField(
                query = query,
                onQueryChange = { query = it },
                label = "Add recipes or ingredients"
            )
        }
        item { Text("Add recipes") }
        if (filteredRecipes.isEmpty()) {
            item { Text(if (query.isBlank()) "No recipes yet." else "No matching recipes.") }
        } else {
            items(filteredRecipes, key = { "recipe-${it.recipe.id}" }) { recipe ->
                TextButton(onClick = { onGenerate(recipe, 1.0) }) {
                    Text("+ ${recipe.recipe.name}")
                }
            }
        }
        item { Text("Add individual ingredients") }
        if (filteredIngredients.isEmpty()) {
            item { Text(if (query.isBlank()) "No ingredients yet." else "No matching ingredients.") }
        } else {
            items(filteredIngredients, key = { "ingredient-${it.id}" }) { ingredient ->
                TextButton(onClick = { onAddIngredient(ingredient) }) {
                    Text("+ ${ingredient.name} (${ingredient.referenceAmount} ${ingredient.referenceUnit})")
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Shopping list")
                Button(onClick = onClear, enabled = items.isNotEmpty()) { Text("Clear list") }
            }
        }
        if (items.isEmpty()) {
            item { Text("Your grocery list is empty.") }
        }
        items(items, key = { it.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(item.checked, { onToggle(item) })
                    Column {
                        Text(item.name)
                        Text("${item.amount} ${item.unit}")
                    }
                }
            }
        }
    }
}