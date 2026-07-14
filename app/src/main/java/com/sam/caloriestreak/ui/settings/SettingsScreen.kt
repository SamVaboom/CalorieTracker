package com.sam.caloriestreak.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    Column(Modifier.padding(16.dp)) {
        Text("Settings")
        OutlinedTextField(
            value = calorieValue,
            onValueChange = { calorieValue = it.filter(Char::isDigit) },
            label = { Text("Daily calorie target") },
            supportingText = { Text("Accepted range: 800–5000 kcal") }
        )
        OutlinedTextField(
            value = weightValue,
            onValueChange = { weightValue = it.filter { char -> char.isDigit() || char == '.' || char == ',' }.replace(',', '.') },
            label = { Text("Weight goal (kg)") },
            supportingText = { Text("Optional. Accepted range: 20–500 kg") },
            modifier = Modifier.padding(top = 12.dp)
        )
        Button(onClick = {
            val calorie = calorieValue.toIntOrNull()
            val weight = weightValue.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
            message = when {
                calorie == null -> "Enter a whole-number calorie target"
                weightValue.isNotBlank() && weight == null -> "Enter a valid weight goal"
                else -> onSave(calorie, weight).fold({ "Goals saved" }, { it.message ?: "Could not save goals" })
            }
        }) { Text("Save goals") }
        message?.let { Text(it) }
        Text("Freeze earning rule", Modifier.padding(top = 20.dp))
        Text("A score of 85% or higher adds one qualifying day. $freezeRequiredDays qualifying days earn one freeze.")
        Text("A maximum of 3 freezes can be stored. Progress does not accumulate while storage is full.")
    }
}
