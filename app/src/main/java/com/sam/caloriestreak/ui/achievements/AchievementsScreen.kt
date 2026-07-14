package com.sam.caloriestreak.ui.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity
import com.sam.caloriestreak.domain.achievement.AchievementRegistry

private enum class Filter { ALL, EARNED, LOCKED }

@Composable
fun AchievementsScreen(earned: List<EarnedAchievementEntity>, onSeen: () -> Unit) {
    var filter by remember { mutableStateOf(Filter.ALL) }
    val earnedById = earned.associateBy { it.achievementId }
    LaunchedEffect(Unit) { onSeen() }
    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Achievements")
            Text("${earned.size} of ${AchievementRegistry.all.size} earned")
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Filter.entries.forEach { option ->
                    FilterChip(selected = filter == option, onClick = { filter = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }
        }
        val visible = AchievementRegistry.all.sortedBy { it.sortOrder }.filter { definition ->
            when (filter) {
                Filter.ALL -> true
                Filter.EARNED -> definition.id in earnedById
                Filter.LOCKED -> definition.id !in earnedById
            }
        }
        items(visible, key = { it.id }) { definition ->
            val record = earnedById[definition.id]
            val locked = record == null
            Card {
                ListItem(
                    headlineContent = { Text(if (definition.hidden && locked) "Secret achievement" else definition.title) },
                    supportingContent = { Text(if (definition.hidden && locked) "Keep tracking to discover this achievement." else definition.description) },
                    overlineContent = { Text(definition.category.name.replace('_', ' ')) },
                    trailingContent = { Text(if (locked) "Locked" else if (!record.seen) "New" else "Earned") }
                )
            }
        }
    }
}
