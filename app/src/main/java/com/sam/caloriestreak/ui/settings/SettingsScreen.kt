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
fun SettingsScreen(target: Double, freezeRequiredDays: Int, onSaveTarget: (Int) -> Result<Unit>) {
    var value by remember(target) { mutableStateOf(target.toInt().toString()) }
    var message by remember { mutableStateOf<String?>(null) }
    Column(Modifier.padding(16.dp)) {
        Text("Settings")
        OutlinedTextField(
            value = value,
            onValueChange = { value = it.filter(Char::isDigit) },
            label = { Text("Daily calorie target") },
            supportingText = { Text("Accepted range: 800–5000 kcal") }
        )
        Button(onClick = {
            val parsed = value.toIntOrNull()
            message = if (parsed == null) "Enter a whole-number target" else onSaveTarget(parsed).fold({ "Target saved" }, { it.message ?: "Could not save target" })
        }) { Text("Save target") }
        message?.let { Text(it) }
        Text("Freeze earning rule", Modifier.padding(top = 20.dp))
        Text("A score of 85% or higher adds one qualifying day. $freezeRequiredDays qualifying days earn one freeze.")
    }
}
