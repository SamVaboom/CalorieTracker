package com.sam.caloriestreak.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.ui.AppUiState
import com.sam.caloriestreak.ui.components.MealLogRow
import java.time.LocalDate
import kotlin.math.abs

@Composable
fun DashboardScreen(
    state: AppUiState,
    onHistory: () -> Unit,
    onStatistics: () -> Unit,
    onFreezeToday: () -> Unit,
    onDeleteMeal: (MealLogEntity) -> Unit
) {
    val todayMeals = state.meals.filter { it.dateEpochDay == LocalDate.now().toEpochDay() }
    var confirmFreeze by remember { mutableStateOf(false) }
    val difference = state.todayCalories - state.target
    val calorieStatus = when {
        state.todayFrozen -> "Freeze active"
        state.todayCalories == 0.0 -> "No food logged yet"
        abs(difference) < 1.0 -> "Target reached"
        difference < 0 -> "${abs(difference).toInt()} kcal below target"
        else -> "${difference.toInt()} kcal above target"
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.AcUnit,
                    contentDescription = "Available streak freezes",
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = state.freezes.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ScoreRing(
                    score = state.todayEffectiveScore,
                    calorieStatus = calorieStatus
                )
            }
        }
        if (state.todayFrozen) {
            item {
                Text(
                    text = "Actual calorie score: ${state.todayScore.toInt()}% · ${state.todayCalories.toInt()} kcal",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DashboardStat(
                    icon = { Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null) },
                    value = "${state.currentStreak}",
                    label = "Current streak"
                )
                DashboardStat(
                    icon = { Icon(Icons.Outlined.EmojiEvents, contentDescription = null) },
                    value = "${state.bestStreak}",
                    label = "Best streak"
                )
                DashboardStat(
                    icon = { Icon(Icons.Outlined.Timelapse, contentDescription = null) },
                    value = "${state.freezeProgress} / ${state.freezeRequiredDays}",
                    label = "Freeze progress"
                )
            }
        }
        item {
            Button(
                onClick = { confirmFreeze = true },
                enabled = state.freezes > 0 && !state.todayFrozen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.AcUnit, contentDescription = null)
                Text(
                    text = if (state.todayFrozen) "Freeze active today" else "Freeze Today",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(onClick = onHistory, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.History, contentDescription = null)
                    Text("History", Modifier.padding(start = 6.dp))
                }
                Button(onClick = onStatistics, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.BarChart, contentDescription = null)
                    Text("Statistics", Modifier.padding(start = 6.dp))
                }
            }
        }
        item {
            Text(
                text = "Today's meals",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (todayMeals.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        "No food logged yet.",
                        modifier = Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(todayMeals, key = { it.id }) { meal ->
                MealLogRow(meal = meal, onDelete = onDeleteMeal)
            }
        }
    }

    if (confirmFreeze) {
        AlertDialog(
            onDismissRequest = { confirmFreeze = false },
            title = { Text("Freeze today?") },
            text = {
                Text(
                    "One streak freeze will be consumed. Today's real calories and actual score remain visible, but the effective streak score becomes 100%."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmFreeze = false
                    onFreezeToday()
                }) { Text("Use one freeze") }
            },
            dismissButton = {
                TextButton(onClick = { confirmFreeze = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DashboardStat(
    icon: @Composable () -> Unit,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        icon()
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
