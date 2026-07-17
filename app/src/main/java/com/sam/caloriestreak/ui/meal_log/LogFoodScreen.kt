package com.sam.caloriestreak.ui.meal_log

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.domain.search.SearchMatcher
import com.sam.caloriestreak.ui.RecipeSummary
import com.sam.caloriestreak.ui.components.AppSearchField
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import kotlinx.coroutines.launch

private enum class RecipeFilter { ALL, FAVORITES }

@Composable
fun LogFoodScreen(
    recipes: List<RecipeSummary>,
    onLogRecipe: (RecipeSummary, Double, String) -> Unit,
    onManual: (String, Double) -> Unit
) {
    var manualDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(RecipeFilter.ALL) }
    var lastLogAt by remember { mutableLongStateOf(0L) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val filteredRecipes = remember(recipes, query, filter) {
        recipes.filter { summary ->
            (filter == RecipeFilter.ALL || summary.recipe.favorite) && SearchMatcher.matches(
                query,
                summary.recipe.name,
                summary.recipe.description,
                *summary.items.map { it.ingredientName }.toTypedArray()
            )
        }.sortedWith(compareByDescending<RecipeSummary> { it.recipe.favorite }.thenBy { it.recipe.name.lowercase() })
    }

    fun quickLog(summary: RecipeSummary, multiplier: Double, label: String) {
        val now = System.currentTimeMillis()
        if (now - lastLogAt < 650L) return
        lastLogAt = now
        onLogRecipe(summary, multiplier, label)
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        val calories = (summary.totalCalories * multiplier).toInt()
        scope.launch { snackbarHostState.showSnackbar("${summary.recipe.name} · $calories kcal added") }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { manualDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add manual calories")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(
                start = AppDimensions.ScreenPadding,
                top = AppDimensions.Space16,
                end = AppDimensions.ScreenPadding,
                bottom = 104.dp
            ),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)
        ) {
            item { AppSectionHeader("Log Food", subtitle = "Quickly add a saved recipe or manual calories") }
            item {
                AppSearchField(query = query, onQueryChange = { query = it }, label = "Search recipes")
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                    FilterChip(selected = filter == RecipeFilter.ALL, onClick = { filter = RecipeFilter.ALL }, label = { Text("All") })
                    FilterChip(
                        selected = filter == RecipeFilter.FAVORITES,
                        onClick = { filter = RecipeFilter.FAVORITES },
                        label = { Text("Favorites") },
                        leadingIcon = { Icon(Icons.Outlined.Favorite, contentDescription = null) }
                    )
                }
            }
            if (filteredRecipes.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(AppDimensions.Space24),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.RestaurantMenu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                if (query.isBlank()) "No recipes yet" else "No recipes match your search",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = AppDimensions.Space8)
                            )
                        }
                    }
                }
            }
            items(filteredRecipes, key = { it.recipe.id }) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, if (summary.recipe.favorite) AppColors.Violet.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(Modifier.padding(AppDimensions.Space16)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(summary.recipe.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${summary.caloriesPerServing.toInt()} kcal per serving · ${summary.items.size} ingredients",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (summary.recipe.favorite) {
                                Icon(Icons.Outlined.Favorite, contentDescription = "Favorite recipe", tint = AppColors.Violet)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = AppDimensions.Space8),
                            horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space4)
                        ) {
                            TextButton(onClick = { quickLog(summary, 1.0 / summary.recipe.servings, "1 serving") }) { Text("1 serving") }
                            TextButton(onClick = { quickLog(summary, 0.5, "Half") }) { Text("½ recipe") }
                            TextButton(onClick = { quickLog(summary, 1.0, "Full") }) { Text("Full") }
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
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch { snackbarHostState.showSnackbar("$description · ${calories.toInt()} kcal added") }
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text("Manual calorie entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)) {
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(calories, { calories = it }, label = { Text("Calories") }, suffix = { Text("kcal") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onSave(description.trim(), calories.toDouble()) }) { Text("Log") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
