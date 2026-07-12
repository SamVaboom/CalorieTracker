package com.sam.caloriestreak.ui.grocery

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.GroceryItemEntity
import com.sam.caloriestreak.ui.RecipeSummary

@Composable
fun GroceryScreen(
    recipes: List<RecipeSummary>,
    items: List<GroceryItemEntity>,
    onGenerate: (RecipeSummary, Double) -> Unit,
    onToggle: (GroceryItemEntity) -> Unit,
    onClear: () -> Unit
) {
    LazyColumn(Modifier.padding(16.dp)) {
        item {
            Text("Add recipes to shopping list")
            recipes.forEach { recipe ->
                TextButton(onClick = { onGenerate(recipe, 1.0) }) { Text("+ ${recipe.recipe.name}") }
            }
            Button(onClick = onClear, enabled = items.isNotEmpty()) { Text("Clear list") }
        }
        items(items) { item ->
            Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(Modifier.padding(12.dp)) {
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
