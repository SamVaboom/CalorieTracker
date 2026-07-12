package com.sam.caloriestreak.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.ui.AppUiState
import com.sam.caloriestreak.ui.components.MealLogRow
import java.time.LocalDate

@Composable
fun DashboardScreen(
    state: AppUiState,
    onLogFood: () -> Unit,
    onIngredients: () -> Unit,
    onHistory: () -> Unit,
    onStatistics: () -> Unit,
    onDeleteMeal: (MealLogEntity) -> Unit
) {
    val todayMeals = state.meals.filter { it.dateEpochDay == LocalDate.now().toEpochDay() }
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Today: ${state.todayCalories.toInt()} kcal")
            Text("Score: ${state.todayScore.toInt()}% · ${state.status}")
            val difference = state.todayCalories - state.target
            Text(if (difference < 0) "${(-difference).toInt()} kcal below target" else "${difference.toInt()} kcal above target")
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Current streak: ${state.currentStreak} days")
                    Text("Best streak: ${state.bestStreak} days")
                    Text("Freezes: ${state.freezes}")
                    Text("Freeze progress: ${state.freezeProgress} / 5")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLogFood) { Text("Log Food") }
                Button(onClick = onIngredients) { Text("Ingredients") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onHistory) { Text("History") }
                Button(onClick = onStatistics) { Text("Statistics") }
            }
        }
        item { Text("Today's meals") }
        if (todayMeals.isEmpty()) {
            item { Text("No food logged yet.") }
        } else {
            items(todayMeals, key = { it.id }) { meal ->
                MealLogRow(meal = meal, onDelete = onDeleteMeal)
            }
        }
    }
}