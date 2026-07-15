package com.sam.caloriestreak.ui.grocery

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.GroceryItemEntity
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.domain.search.SearchMatcher
import com.sam.caloriestreak.ui.RecipeSummary
import com.sam.caloriestreak.ui.components.AppCard
import com.sam.caloriestreak.ui.components.AppEmptyState
import com.sam.caloriestreak.ui.components.AppSearchField
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions

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
    val haptics = LocalHapticFeedback.current
    val filteredRecipes = remember(recipes, query) {
        recipes.filter { SearchMatcher.matches(query, it.recipe.name) }.sortedBy { it.recipe.name.lowercase() }
    }
    val filteredIngredients = remember(ingredients, query) {
        ingredients.filter { SearchMatcher.matches(query, it.name, it.brand, it.category) }.sortedBy { it.name.lowercase() }
    }
    val checkedCount = items.count { it.checked }
    val progress = if (items.isEmpty()) 0f else checkedCount.toFloat() / items.size

    LazyColumn(
        contentPadding = PaddingValues(AppDimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)
    ) {
        item { AppSectionHeader("Grocery Lists", subtitle = "Build a checklist from recipes or individual ingredients") }
        item { AppSearchField(query = query, onQueryChange = { query = it }, label = "Search recipes or ingredients") }

        if (query.isNotBlank()) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Text("Quick add", style = MaterialTheme.typography.titleMedium)
                    if (filteredRecipes.isEmpty() && filteredIngredients.isEmpty()) {
                        Text("No matching recipes or ingredients.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = AppDimensions.Space8))
                    }
                    filteredRecipes.take(5).forEach { recipe ->
                        TextButton(onClick = { onGenerate(recipe, 1.0) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Add recipe · ${recipe.recipe.name}", modifier = Modifier.fillMaxWidth())
                        }
                    }
                    filteredIngredients.take(8).forEach { ingredient ->
                        TextButton(onClick = { onAddIngredient(ingredient) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Add ingredient · ${ingredient.name}", modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        } else {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                    recipes.take(3).forEach { recipe ->
                        TextButton(onClick = { onGenerate(recipe, 1.0) }, modifier = Modifier.weight(1f)) {
                            Text(recipe.recipe.name, maxLines = 2)
                        }
                    }
                }
            }
        }

        item {
            AppCard(Modifier.fillMaxWidth(), emphasized = items.isNotEmpty() && checkedCount == items.size) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Shopping list", style = MaterialTheme.typography.titleLarge)
                        Text(
                            if (items.isEmpty()) "No active items" else "$checkedCount of ${items.size} checked",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(onClick = onClear, enabled = items.isNotEmpty()) { Text("Clear") }
                }
                if (items.isNotEmpty()) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().padding(top = AppDimensions.Space12),
                        color = if (progress >= 1f) AppColors.Success else AppColors.Weight,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                    if (progress >= 1f) {
                        Row(Modifier.padding(top = AppDimensions.Space12), verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = AppColors.Success)
                            Text("List complete", color = AppColors.Success, modifier = Modifier.padding(start = AppDimensions.Space8))
                        }
                    }
                }
            }
        }

        if (items.isEmpty()) {
            item {
                AppEmptyState(
                    icon = Icons.Outlined.LocalGroceryStore,
                    title = "Your list is empty",
                    message = "Search above to add a recipe or ingredient."
                )
            }
        }

        items(items.sortedBy { it.checked }, key = { it.id }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.checked) MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.62f) else MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, if (item.checked) AppColors.Success.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(AppDimensions.Space12),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.checked,
                        onCheckedChange = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggle(item)
                        }
                    )
                    Column(Modifier.padding(start = AppDimensions.Space8)) {
                        Text(
                            item.name,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${item.amount} ${item.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
