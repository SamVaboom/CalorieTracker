package com.sam.caloriestreak.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MoreScreen(
    latestWeight: Double?,
    earnedCount: Int,
    totalCount: Int,
    unseenCount: Int,
    onGrocery: () -> Unit,
    onWeight: () -> Unit,
    onAchievements: () -> Unit,
    onSettings: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("More", style = MaterialTheme.typography.headlineMedium)
        MoreRow("Grocery Lists", "Build and check shopping lists", onGrocery)
        MoreRow("Weight", latestWeight?.let { "Latest: %.1f kg".format(it) } ?: "No weight recorded", onWeight)
        MoreRow("Achievements", "$earnedCount of $totalCount earned${if (unseenCount > 0) " · $unseenCount new" else ""}", onAchievements)
        MoreRow("Settings", "Calorie target and freeze rules", onSettings)
    }
}

@Composable
private fun MoreRow(title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick) { ListItem(headlineContent = { Text(title) }, supportingContent = { Text(subtitle) }) }
}
