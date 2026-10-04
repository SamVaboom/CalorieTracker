package com.sam.caloriestreak.ui.ingredients

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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.domain.editing.IngredientDraft
import com.sam.caloriestreak.domain.search.SearchMatcher
import com.sam.caloriestreak.ui.components.AppEmptyState
import com.sam.caloriestreak.ui.components.AppSearchField
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import kotlinx.coroutines.launch

@Composable
fun IngredientsScreen(
    ingredients: List<IngredientEntity>,
    onSave: (IngredientEntity?, IngredientDraft) -> Unit,
    onDelete: (IngredientEntity) -> Unit,
    onOpenRecipes: () -> Unit
) {
    var editing by remember { mutableStateOf<IngredientEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showArchived by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<IngredientEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val filtered = remember(ingredients, query, showArchived) {
        ingredients.filter { showArchived || !it.archived }
            .filter { SearchMatcher.matches(query, it.name, it.brand, it.category) }
            .sortedWith(compareByDescending<IngredientEntity> { it.favorite }.thenBy { it.archived }.thenBy { it.name.lowercase() })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) { Icon(Icons.Default.Add, contentDescription = "Add ingredient") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = AppDimensions.ScreenPadding, top = AppDimensions.Space16, end = AppDimensions.ScreenPadding, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        AppSectionHeader("Ingredients", subtitle = "Reusable calorie references for recipes")
                    }
                    IconButton(onClick = onOpenRecipes) { Icon(Icons.Outlined.MenuBook, contentDescription = "Open recipes") }
                }
            }
            item { AppSearchField(query = query, onQueryChange = { query = it }, label = "Search ingredients") }
            item {
                FilterChip(
                    selected = showArchived,
                    onClick = { showArchived = !showArchived },
                    label = { Text("Show archived") }
                )
            }
            if (filtered.isEmpty()) {
                item {
                    AppEmptyState(
                        icon = Icons.Outlined.Inventory2,
                        title = if (query.isBlank()) "No ingredients yet" else "No matching ingredients",
                        message = if (query.isBlank()) "Add an ingredient to start building recipes." else "Try another search term."
                    )
                }
            }
            items(filtered, key = { it.id }) { ingredient ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, if (ingredient.favorite) AppColors.Violet.copy(alpha = 0.42f) else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(Modifier.fillMaxWidth().padding(AppDimensions.Space16), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppDimensions.Space4)) {
                            Text(
                                ingredient.name + if (ingredient.archived) " · Archived" else "",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (ingredient.archived) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                            ingredient.brand?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            ingredient.category?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.labelMedium, color = AppColors.Cyan)
                            }
                            Text(
                                "${ingredient.calories.toInt()} kcal / ${ingredient.referenceAmount} ${ingredient.referenceUnit}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.Coral
                            )
                        }
                        IconButton(onClick = { editing = ingredient; showDialog = true }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit ${ingredient.name}")
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
            existing = editing,
            onDismiss = { showDialog = false },
            onSave = { draft ->
                val wasEditing = editing != null
                onSave(editing, draft)
                showDialog = false
                scope.launch { snackbarHostState.showSnackbar(if (wasEditing) "Ingredient updated" else "Ingredient added") }
            }
        )
    }

    pendingDelete?.let { ingredient ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Delete ingredient?") },
            text = { Text("Delete ${ingredient.name}? If a recipe uses it, it will be archived instead so the recipe remains valid.") },
            confirmButton = {
                TextButton(onClick = { pendingDelete = null; onDelete(ingredient) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun IngredientDialog(
    existing: IngredientEntity?,
    onDismiss: () -> Unit,
    onSave: (IngredientDraft) -> Unit
) {
    val initial = existing?.let(IngredientDraft::from) ?: IngredientDraft()
    var name by remember(existing?.id) { mutableStateOf(initial.name) }
    var brand by remember(existing?.id) { mutableStateOf(initial.brand) }
    var calories by remember(existing?.id) { mutableStateOf(initial.calories.toString()) }
    var amount by remember(existing?.id) { mutableStateOf(initial.referenceAmount.toString()) }
    var unit by remember(existing?.id) { mutableStateOf(initial.referenceUnit) }
    var category by remember(existing?.id) { mutableStateOf(initial.category) }
    var favorite by remember(existing?.id) { mutableStateOf(initial.favorite) }
    var archived by remember(existing?.id) { mutableStateOf(initial.archived) }
    var saving by remember(existing?.id) { mutableStateOf(false) }
    val caloriesValue = calories.toDoubleOrNull()
    val amountValue = amount.toDoubleOrNull()
    val valid = name.isNotBlank() && caloriesValue != null && caloriesValue >= 0.0 && amountValue != null && amountValue > 0.0 && unit.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(if (existing == null) "Add Ingredient" else "Edit Ingredient") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)) {
                item { Text("Basic information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                item { OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(brand, { brand = it }, label = { Text("Brand") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(category, { category = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { Text("Calorie basis", style = MaterialTheme.typography.titleMedium, color = AppColors.Coral, modifier = Modifier.padding(top = AppDimensions.Space8)) }
                item { OutlinedTextField(calories, { calories = it }, label = { Text("Calories") }, suffix = { Text("kcal") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(amount, { amount = it }, label = { Text("Reference amount") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(unit, { unit = it }, label = { Text("Reference unit") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
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
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && !saving,
                onClick = {
                    saving = true
                    onSave(
                        IngredientDraft(
                            name = name,
                            brand = brand,
                            calories = requireNotNull(caloriesValue),
                            referenceAmount = requireNotNull(amountValue),
                            referenceUnit = unit,
                            category = category,
                            favorite = favorite,
                            archived = archived
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancel") } }
    )
}
