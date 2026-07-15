package com.sam.caloriestreak.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sam.caloriestreak.ui.components.AppCard
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.theme.AppDimensions

@Composable
fun SettingsScreen(
    calorieTarget: Double,
    weightGoal: Double?,
    freezeRequiredDays: Int,
    onSave: (Int, Double?) -> Result<Unit>
) {
    var calorieValue by remember(calorieTarget) { mutableStateOf(calorieTarget.toInt().toString()) }
    var weightValue by remember(weightGoal) { mutableStateOf(weightGoal?.toString().orEmpty()) }
    var message by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(AppDimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Space16)
    ) {
        item { AppSectionHeader("Settings", subtitle = "Goals, scoring and app preferences") }
        item {
            AppSectionHeader("Goals")
            AppCard(Modifier.fillMaxWidth().padding(top = AppDimensions.Space8)) {
                OutlinedTextField(
                    value = calorieValue,
                    onValueChange = { calorieValue = it.filter(Char::isDigit) },
                    label = { Text("Daily calorie target") },
                    suffix = { Text("kcal") },
                    supportingText = { Text("Accepted range: 800–5000 kcal") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = weightValue,
                    onValueChange = { weightValue = it.filter { char -> char.isDigit() || char == '.' || char == ',' }.replace(',', '.') },
                    label = { Text("Weight goal") },
                    suffix = { Text("kg") },
                    supportingText = { Text("Optional. Accepted range: 20–500 kg") },
                    modifier = Modifier.fillMaxWidth().padding(top = AppDimensions.Space12),
                    shape = MaterialTheme.shapes.medium
                )
                Button(
                    onClick = {
                        val calorie = calorieValue.toIntOrNull()
                        val weight = weightValue.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
                        message = when {
                            calorie == null -> "Enter a whole-number calorie target"
                            weightValue.isNotBlank() && weight == null -> "Enter a valid weight goal"
                            else -> onSave(calorie, weight).fold({ "Goals saved" }, { it.message ?: "Could not save goals" })
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = AppDimensions.Space16)
                ) { Text("Save goals") }
                message?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = AppDimensions.Space8)
                    )
                }
            }
        }
        item {
            AppSectionHeader("Streaks & Freezes")
            AppCard(Modifier.fillMaxWidth().padding(top = AppDimensions.Space8)) {
                Text("Freeze earning rule", style = MaterialTheme.typography.titleMedium)
                Text(
                    "A score of 85% or higher adds one qualifying day. $freezeRequiredDays qualifying days earn one freeze.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AppDimensions.Space8)
                )
                Text(
                    "A maximum of 3 freezes can be stored. Progress does not accumulate while storage is full.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AppDimensions.Space8)
                )
            }
        }
    }
}
