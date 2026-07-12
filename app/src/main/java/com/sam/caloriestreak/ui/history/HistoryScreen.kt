package com.sam.caloriestreak.ui.history

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.ui.components.MealLogRow
import java.time.LocalDate

@Composable
fun HistoryScreen(meals: List<MealLogEntity>, onDelete: (MealLogEntity) -> Unit) {
    val grouped = meals.groupBy { it.dateEpochDay }.toSortedMap(compareByDescending { it })
    LazyColumn(Modifier.padding(16.dp)) {
        if (grouped.isEmpty()) {
            item { Text("No meal history yet.") }
        }
        grouped.forEach { (day, dayMeals) ->
            item(key = "day-$day") {
                Text(
                    "${LocalDate.ofEpochDay(day)} · ${dayMeals.sumOf { it.calories }.toInt()} kcal",
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }
            items(dayMeals, key = { it.id }) { meal ->
                MealLogRow(
                    meal = meal,
                    onDelete = onDelete,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}