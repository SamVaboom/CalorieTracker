package com.sam.caloriestreak.ui.recipes

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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.domain.editing.RecipeDraft
import com.sam.caloriestreak.domain.editing.RecipeIngredientDraft
import com.sam.caloriestreak.domain.editing.UnitConverter
import com.sam.caloriestreak.domain.search.SearchMatcher
import com.sam.caloriestreak.ui.RecipeSummary
import com.sam.caloriestreak.ui.components.AppEmptyState
import com.sam.caloriestreak.ui.components.AppSearchField
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import kotlinx.coroutines.launch

@Composable
fun RecipesScreen(
    ingredients: List<IngredientEntity>,
    recipes: List<RecipeSummary>,
    onSave: (RecipeSummary?, RecipeDraft) -> Unit,
    onOpenIngredients: () -> Unit
) {
    var editing by remember { mutableStateOf<RecipeSummary?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showArchived by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val filteredRecipes = remember(recipes, query, showArchived) {
        recipes.filter { showArchived || !it.recipe.archived }
            .filter { SearchMatcher.matches(query, it.recipe.name, it.recipe.description) }
            .sortedWith(compareByDescending<RecipeSummary> { it.recipe.favorite }.thenBy { it.recipe.archived }.thenBy { it.recipe.name.lowercase() })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) { Icon(Icons.Default.Add, contentDescription = "Add recipe") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = AppDimensions.ScreenPadding, top = AppDimensions.Space16, end = AppDimensions.ScreenPadding, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { AppSectionHeader("Recipes", subtitle = "Reusable meals built from your ingredients") }
                    IconButton(onClick = onOpenIngredients) { Icon(Icons.Outlined.Inventory2, contentDescription = "Open ingredients") }
                }
            }
            item { AppSearchField(query = query, onQueryChange = { query = it }, label = "Search recipes") }
            item { FilterChip(selected = showArchived, onClick = { showArchived = !showArchived }, label = { Text("Show archived") }) }
            if (ingredients.none { !it.archived }) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Add an active ingredient before creating a recipe.", modifier = Modifier.padding(AppDimensions.Space16))
                    }
                }
            }
            if (filteredRecipes.isEmpty()) {
                item {
                    AppEmptyState(
                        icon = Icons.Outlined.MenuBook,
                        title = if (query.isBlank()) "No recipes yet" else "No matching recipes",
                        message = if (query.isBlank()) "Create a recipe to make daily logging faster." else "Try another search term."
                    )
                }
            }
            items(filteredRecipes, key = { it.recipe.id }) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, if (summary.recipe.favorite) AppColors.Violet.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(Modifier.fillMaxWidth().padding(AppDimensions.Space16), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppDimensions.Space4)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    summary.recipe.name + if (summary.recipe.archived) " · Archived" else "",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (summary.recipe.favorite) {
                                    Icon(Icons.Outlined.Favorite, contentDescription = "Favorite recipe", tint = AppColors.Violet, modifier = Modifier.padding(start = AppDimensions.Space8))
                                }
                            }
                            summary.recipe.description?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "${summary.caloriesPerServing.toInt()} kcal per serving",
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppColors.Coral
                            )
                            Text(
                                "${summary.items.size} ingredients · ${summary.recipe.servings} servings · ${summary.totalCalories.toInt()} kcal total",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { editing = summary; showDialog = true }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit ${summary.recipe.name}")
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        RecipeDialog(
            ingredients = ingredients,
            existing = editing,
            onDismiss = { showDialog = false },
            onSave = { draft ->
                val wasEditing = editing != null
                onSave(editing, draft)
                showDialog = false
                scope.launch { snackbarHostState.showSnackbar(if (wasEditing) "Recipe updated" else "Recipe added") }
            }
        )
    }
}

@Composable
private fun RecipeDialog(
    ingredients: List<IngredientEntity>,
    existing: RecipeSummary?,
    onDismiss: () -> Unit,
    onSave: (RecipeDraft) -> Unit
) {
    val initial = existing?.let { RecipeDraft.from(it.recipe, it.items) } ?: RecipeDraft()
    var name by remember(existing?.recipe?.id) { mutableStateOf(initial.name) }
    var description by remember(existing?.recipe?.id) { mutableStateOf(initial.description) }
    var servings by remember(existing?.recipe?.id) { mutableStateOf(initial.servings.toString()) }
    var favorite by remember(existing?.recipe?.id) { mutableStateOf(initial.favorite) }
    var archived by remember(existing?.recipe?.id) { mutableStateOf(initial.archived) }
    var ingredientQuery by remember(existing?.recipe?.id) { mutableStateOf("") }
    var showArchivedIngredients by remember(existing?.recipe?.id) { mutableStateOf(false) }
    var confirmDiscard by remember(existing?.recipe?.id) { mutableStateOf(false) }
    var saving by remember(existing?.recipe?.id) { mutableStateOf(false) }
    val selected = remember(existing?.recipe?.id) { mutableStateMapOf<String, Boolean>().apply { initial.items.forEach { put(it.ingredientId, true) } } }
    val amounts = remember(existing?.recipe?.id) { mutableStateMapOf<String, String>().apply { initial.items.forEach { put(it.ingredientId, it.amount.toString()) } } }
    val units = remember(existing?.recipe?.id) { mutableStateMapOf<String, String>().apply { initial.items.forEach { put(it.ingredientId, it.unit) } } }
    val notes = remember(existing?.recipe?.id) { mutableStateMapOf<String, String>().apply { initial.items.forEach { put(it.ingredientId, it.note) } } }

    val selectedItems = ingredients.filter { selected[it.id] == true }.map { ingredient ->
        RecipeIngredientDraft(
            ingredientId = ingredient.id,
            amount = amounts[ingredient.id]?.toDoubleOrNull() ?: 0.0,
            unit = units[ingredient.id] ?: ingredient.referenceUnit,
            note = notes[ingredient.id].orEmpty()
        )
    }
    val servingsValue = servings.toDoubleOrNull() ?: 0.0
    val draft = RecipeDraft(name, description, servingsValue, favorite, archived, selectedItems)
    val totalCalories = draft.totalCalories(ingredients)
    val valid = draft.isValid(ingredients)
    val dirty = draft != initial
    val filteredIngredients = remember(ingredients, ingredientQuery, showArchivedIngredients, selected.toMap()) {
        ingredients.filter { !it.archived || showArchivedIngredients || selected[it.id] == true }
            .filter { SearchMatcher.matches(ingredientQuery, it.name, it.brand, it.category) }
            .sortedWith(compareBy<IngredientEntity> { it.archived }.thenBy { it.name.lowercase() })
    }

    fun requestDismiss() { if (dirty) confirmDiscard = true else onDismiss() }

    AlertDialog(
        onDismissRequest = { if (!saving) requestDismiss() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(if (existing == null) "Add Recipe" else "Edit Recipe") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)) {
                item { Text("Basic information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                item { OutlinedTextField(name, { name = it }, label = { Text("Recipe name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(servings, { servings = it }, label = { Text("Default servings") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = MaterialTheme.shapes.medium) {
                        Text(
                            "${totalCalories.toInt()} kcal total" + if (servingsValue > 0.0) " · ${(totalCalories / servingsValue).toInt()} per serving" else "",
                            modifier = Modifier.padding(AppDimensions.Space12),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = favorite, onCheckedChange = { favorite = it }); Text("Favorite")
                        Checkbox(checked = archived, onCheckedChange = { archived = it }, modifier = Modifier.padding(start = AppDimensions.Space12)); Text("Archived")
                    }
                }
                item { Text("Recipe ingredients", style = MaterialTheme.typography.titleMedium, color = AppColors.Coral, modifier = Modifier.padding(top = AppDimensions.Space8)) }
                item { AppSearchField(query = ingredientQuery, onQueryChange = { ingredientQuery = it }, label = "Search ingredients") }
                item { FilterChip(selected = showArchivedIngredients, onClick = { showArchivedIngredients = !showArchivedIngredients }, label = { Text("Show archived ingredients") }) }
                if (filteredIngredients.isEmpty()) item { Text("No ingredients match your search.") }
                items(filteredIngredients, key = { it.id }) { ingredient ->
                    val chosen = selected[ingredient.id] == true
                    val unitValue = units[ingredient.id] ?: ingredient.referenceUnit
                    val compatible = UnitConverter.areCompatible(unitValue, ingredient.referenceUnit)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (chosen) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceContainer),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(Modifier.fillMaxWidth().padding(AppDimensions.Space12), verticalAlignment = Alignment.Top) {
                            Checkbox(
                                checked = chosen,
                                onCheckedChange = { checked ->
                                    selected[ingredient.id] = checked
                                    if (checked && units[ingredient.id] == null) units[ingredient.id] = ingredient.referenceUnit
                                }
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                                Text(ingredient.name + if (ingredient.archived) " · Archived" else "", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(amounts[ingredient.id] ?: "", { amounts[ingredient.id] = it }, label = { Text("Amount") }, enabled = chosen, singleLine = true, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(
                                    unitValue,
                                    { units[ingredient.id] = it },
                                    label = { Text("Unit (reference: ${ingredient.referenceUnit})") },
                                    enabled = chosen,
                                    isError = chosen && !compatible,
                                    supportingText = if (chosen && !compatible) ({ Text("Unit is not compatible with ${ingredient.referenceUnit}") }) else null,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(notes[ingredient.id].orEmpty(), { notes[ingredient.id] = it }, label = { Text("Note") }, enabled = chosen, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !saving, onClick = { saving = true; onSave(draft) }) { Text("Save") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = ::requestDismiss) { Text("Cancel") } }
    )

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Discard changes?") },
            text = { Text("Your unsaved recipe changes will be lost.") },
            confirmButton = { TextButton(onClick = { confirmDiscard = false; onDismiss() }) { Text("Discard changes", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Continue editing") } }
        )
    }
}
