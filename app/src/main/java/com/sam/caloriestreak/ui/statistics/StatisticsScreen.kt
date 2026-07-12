package com.sam.caloriestreak.ui.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.domain.calculation.ScoreCalculator
import java.time.LocalDate

@Composable
fun StatisticsScreen(meals: List<MealLogEntity>, currentStreak: Int, bestStreak: Int) {
    val calculator = ScoreCalculator()
    val byDay = meals.groupBy { it.dateEpochDay }.mapValues { (_, values) -> values.sumOf { it.calories } }
    val today = LocalDate.now().toEpochDay()
    val last7 = (0L..6L).map { byDay[today - it] ?: 0.0 }
    val last30 = (0L..29L).map { byDay[today - it] ?: 0.0 }
    val sevenAverageCalories = last7.average()
    val thirtyAverageCalories = last30.average()
    val sevenAverageScore = last7.map(calculator::calculate).average()
    val thirtyAverageScore = last30.map(calculator::calculate).average()
    Column(Modifier.padding(16.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Current streak: $currentStreak")
                Text("Best streak: $bestStreak")
                Text("7-day average: ${sevenAverageCalories.toInt()} kcal · ${sevenAverageScore.toInt()}%")
                Text("30-day average: ${thirtyAverageCalories.toInt()} kcal · ${thirtyAverageScore.toInt()}%")
                Text("Tracked days: ${byDay.size}")
            }
        }
    }
}
