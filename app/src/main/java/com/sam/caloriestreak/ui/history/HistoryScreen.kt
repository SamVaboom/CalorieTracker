package com.sam.caloriestreak.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import java.time.LocalDate

@Composable
fun HistoryScreen(meals: List<MealLogEntity>, onDelete: (MealLogEntity) -> Unit) {
    val grouped = meals.groupBy { it.dateEpochDay }.toSortedMap(compareByDescending { it })
    LazyColumn(Modifier.padding(16.dp)) {
        grouped.forEach { (day, dayMeals) ->
            item {
                Text("${LocalDate.ofEpochDay(day)} · ${dayMeals.sumOf { it.calories }.toInt()} kcal")
            }
            items(dayMeals) { meal ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(meal.recipeName)
                        Text("${meal.portionDescription} · ${meal.calories.toInt()} kcal")
                        TextButton(onClick = { onDelete(meal) }) { Text("Delete") }
                    }
                }
            }
        }
    }
}
