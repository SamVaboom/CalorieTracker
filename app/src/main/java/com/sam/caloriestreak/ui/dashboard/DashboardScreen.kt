package com.sam.caloriestreak.ui.dashboard

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
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.domain.calculation.ScoreDisplay
import com.sam.caloriestreak.ui.AppUiState
import com.sam.caloriestreak.ui.components.AppCard
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.components.MealLogRow
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun DashboardScreen(
    state: AppUiState,
    onHistory: () -> Unit,
    onStatistics: () -> Unit,
    onFreezeToday: () -> Unit,
    onDeleteMeal: (MealLogEntity) -> Unit
) {
    val today = LocalDate.now()
    val todayMeals = state.meals.filter { it.dateEpochDay == today.toEpochDay() }
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
        contentPadding = PaddingValues(
            start = AppDimensions.ScreenPadding,
            top = AppDimensions.Space16,
            end = AppDimensions.ScreenPadding,
            bottom = AppDimensions.Space32
        ),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Space16)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Today", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    onClick = { if (state.freezes > 0 && !state.todayFrozen) confirmFreeze = true },
                    label = { Text("${state.freezes} / 3") },
                    leadingIcon = { Icon(Icons.Outlined.AcUnit, contentDescription = null, tint = AppColors.Freeze) },
                    modifier = Modifier.semantics { contentDescription = "${state.freezes} streak freezes available" }
                )
            }
        }

        item {
            AppCard(modifier = Modifier.fillMaxWidth(), emphasized = true) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    ScoreRing(
                        score = state.todayEffectiveScore,
                        calories = state.todayCalories,
                        target = state.target,
                        calorieStatus = calorieStatus
                    )
                }
                if (state.todayFrozen) {
                    Text(
                        text = "Actual score: ${ScoreDisplay.percent(state.todayScore)}% · ${state.todayCalories.toInt()} kcal",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)) {
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null) },
                    value = state.currentStreak.toString(),
                    label = "Current",
                    accent = AppColors.Coral
                )
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Outlined.EmojiEvents, contentDescription = null) },
                    value = state.bestStreak.toString(),
                    label = "Best",
                    accent = AppColors.Achievement
                )
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Outlined.Timelapse, contentDescription = null) },
                    value = "${state.freezeProgress}/${state.freezeRequiredDays}",
                    label = "Freeze",
                    accent = AppColors.Freeze
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
                    when {
                        state.todayFrozen -> "Freeze active today"
                        state.freezes == 0 -> "No freezes available"
                        else -> "Freeze Today"
                    },
                    modifier = Modifier.padding(start = AppDimensions.Space8)
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space12)) {
                FilledTonalButton(onClick = onHistory, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.History, contentDescription = null)
                    Text("History", Modifier.padding(start = AppDimensions.Space8))
                }
                FilledTonalButton(onClick = onStatistics, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.BarChart, contentDescription = null)
                    Text("Statistics", Modifier.padding(start = AppDimensions.Space8))
                }
            }
        }

        item { AppSectionHeader("Today's meals", subtitle = "Everything logged for the current day") }
        if (todayMeals.isEmpty()) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = AppDimensions.Space8),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.LocalDining, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Nothing logged yet", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = AppDimensions.Space8))
                        Text(
                            "Your first meal is one tap away.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(todayMeals, key = { it.id }) { meal -> MealLogRow(meal = meal, onDelete = onDeleteMeal) }
        }
    }

    if (confirmFreeze) {
        AlertDialog(
            onDismissRequest = { confirmFreeze = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            title = { Text("Freeze today?") },
            text = { Text("One streak freeze will be consumed. Today's real calories remain visible, while the streak is protected.") },
            confirmButton = { TextButton(onClick = { confirmFreeze = false; onFreezeToday() }) { Text("Use one freeze") } },
            dismissButton = { TextButton(onClick = { confirmFreeze = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DashboardStatCard(
    modifier: Modifier,
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    accent: Color
) {
    Card(
        modifier = modifier.semantics { contentDescription = "$label: $value" },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = AppDimensions.Space16, horizontal = AppDimensions.Space8)
        ) {
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides accent) { icon() }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
