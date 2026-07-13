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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.sam.caloriestreak.ui.components.AppSearchField
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
            .sortedWith(compareBy<RecipeSummary> { it.recipe.archived }.thenBy { it.recipe.name.lowercase() })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editing = null
                    showDialog = true
                },
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
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showArchived, onCheckedChange = { showArchived = it })
                    Text("Show archived")
                }
            }
            if (ingredients.none { !it.archived }) {
                item { Text("Add an active ingredient before creating a recipe.") }
            }
            if (filteredRecipes.isEmpty()) {
                item {
                    Text(if (query.isBlank()) "No recipes yet." else "No recipes match your search.")
                }
            }
            items(filteredRecipes, key = { it.recipe.id }) { summary ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(summary.recipe.name + if (summary.recipe.archived) " · Archived" else "")
                            summary.recipe.description?.takeIf { it.isNotBlank() }?.let { Text(it) }
                            Text("${summary.totalCalories.toInt()} kcal total")
                            Text("${summary.caloriesPerServing.toInt()} kcal per serving")
                            summary.items.forEach { item ->
                                Text("• ${item.amount} ${item.unit} ${item.ingredientName}${item.note?.let { " — $it" }.orEmpty()}")
                            }
                        }
                        IconButton(onClick = {
                            editing = summary
                            showDialog = true
                        }) {
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
                scope.launch {
                    snackbarHostState.showSnackbar(if (wasEditing) "Recipe updated" else "Recipe added")
                }
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
    val selected = remember(existing?.recipe?.id) {
        mutableStateMapOf<String, Boolean>().apply {
            initial.items.forEach { put(it.ingredientId, true) }
        }
    }
    val amounts = remember(existing?.recipe?.id) {
        mutableStateMapOf<String, String>().apply {
            initial.items.forEach { put(it.ingredientId, it.amount.toString()) }
        }
    }
    val units = remember(existing?.recipe?.id) {
        mutableStateMapOf<String, String>().apply {
            initial.items.forEach { put(it.ingredientId, it.unit) }
        }
    }
    val notes = remember(existing?.recipe?.id) {
        mutableStateMapOf<String, String>().apply {
            initial.items.forEach { put(it.ingredientId, it.note) }
        }
    }

    val selectedItems = ingredients.filter { selected[it.id] == true }.map { ingredient ->
        RecipeIngredientDraft(
            ingredientId = ingredient.id,
            amount = amounts[ingredient.id]?.toDoubleOrNull() ?: 0.0,
            unit = units[ingredient.id] ?: ingredient.referenceUnit,
            note = notes[ingredient.id].orEmpty()
        )
    }
    val servingsValue = servings.toDoubleOrNull() ?: 0.0
    val draft = RecipeDraft(
        name = name,
        description = description,
        servings = servingsValue,
        favorite = favorite,
        archived = archived,
        items = selectedItems
    )
    val totalCalories = draft.totalCalories(ingredients)
    val valid = draft.isValid(ingredients)
    val dirty = draft != initial
    val filteredIngredients = remember(ingredients, ingredientQuery, showArchivedIngredients, selected.toMap()) {
        ingredients.filter { ingredient ->
            !ingredient.archived || showArchivedIngredients || selected[ingredient.id] == true
        }.filter {
            SearchMatcher.matches(ingredientQuery, it.name, it.brand, it.category)
        }.sortedWith(compareBy<IngredientEntity> { it.archived }.thenBy { it.name.lowercase() })
    }

    fun requestDismiss() {
        if (dirty) confirmDiscard = true else onDismiss()
    }

    AlertDialog(
        onDismissRequest = { if (!saving) requestDismiss() },
        title = { Text(if (existing == null) "Add Recipe" else "Edit Recipe") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(name, { name = it }, label = { Text("Recipe name") }, singleLine = true) }
                item { OutlinedTextField(description, { description = it }, label = { Text("Description") }) }
                item { OutlinedTextField(servings, { servings = it }, label = { Text("Default servings") }, singleLine = true) }
                item {
                    Text(
                        "${totalCalories.toInt()} kcal total" +
                            if (servingsValue > 0.0) " · ${(totalCalories / servingsValue).toInt()} per serving" else ""
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = favorite, onCheckedChange = { favorite = it })
                        Text("Favorite")
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = archived, onCheckedChange = { archived = it })
                        Text("Archived")
                    }
                }
                item {
                    AppSearchField(
                        query = ingredientQuery,
                        onQueryChange = { ingredientQuery = it },
                        label = "Search ingredients"
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showArchivedIngredients,
                            onCheckedChange = { showArchivedIngredients = it }
                        )
                        Text("Show archived ingredients")
                    }
                }
                if (filteredIngredients.isEmpty()) {
                    item { Text("No ingredients match your search.") }
                }
                items(filteredIngredients, key = { it.id }) { ingredient ->
                    val chosen = selected[ingredient.id] == true
                    val unitValue = units[ingredient.id] ?: ingredient.referenceUnit
                    val compatible = UnitConverter.areCompatible(unitValue, ingredient.referenceUnit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = chosen,
                            onCheckedChange = { checked ->
                                selected[ingredient.id] = checked
                                if (checked && units[ingredient.id] == null) {
                                    units[ingredient.id] = ingredient.referenceUnit
                                }
                            }
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(ingredient.name + if (ingredient.archived) " · Archived" else "")
                            OutlinedTextField(
                                value = amounts[ingredient.id] ?: "",
                                onValueChange = { amounts[ingredient.id] = it },
                                label = { Text("Amount") },
                                enabled = chosen,
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = unitValue,
                                onValueChange = { units[ingredient.id] = it },
                                label = { Text("Unit (reference: ${ingredient.referenceUnit})") },
                                enabled = chosen,
                                isError = chosen && !compatible,
                                supportingText = if (chosen && !compatible) {
                                    { Text("Unit is not compatible with ${ingredient.referenceUnit}") }
                                } else null,
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = notes[ingredient.id].orEmpty(),
                                onValueChange = { notes[ingredient.id] = it },
                                label = { Text("Note") },
                                enabled = chosen
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && !saving,
                onClick = {
                    saving = true
                    onSave(draft)
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = ::requestDismiss) { Text("Cancel") } }
    )

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard changes?") },
            text = { Text("Your unsaved recipe changes will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    onDismiss()
                }) { Text("Discard changes") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text("Continue editing") }
            }
        )
    }
}
