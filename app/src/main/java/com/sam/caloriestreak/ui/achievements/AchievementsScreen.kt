package com.sam.caloriestreak.ui.achievements

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity
import com.sam.caloriestreak.domain.achievement.AchievementCategory
import com.sam.caloriestreak.domain.achievement.AchievementRegistry
import java.text.DateFormat
import java.util.Date

private enum class EarnedFilter { ALL, EARNED, LOCKED }
private val earnedGreen = Color(0xFF2E7D32)

@Composable
fun AchievementsScreen(earned: List<EarnedAchievementEntity>, onSeen: () -> Unit) {
    var filter by remember { mutableStateOf(EarnedFilter.ALL) }
    var category by remember { mutableStateOf<AchievementCategory?>(null) }
    val earnedById = earned.associateBy { it.achievementId }
    LaunchedEffect(Unit) { onSeen() }
    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Achievements")
            Text("${earned.size} of ${AchievementRegistry.all.size} earned")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EarnedFilter.entries.forEach { option ->
                    FilterChip(selected = filter == option, onClick = { filter = option }, label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = category == null, onClick = { category = null }, label = { Text("All categories") })
                AchievementCategory.entries.filterNot { it == AchievementCategory.HIDDEN }.forEach { option ->
                    FilterChip(selected = category == option, onClick = { category = option }, label = { Text(option.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }) })
                }
            }
        }
        val visible = AchievementRegistry.all.sortedBy { it.sortOrder }.filter { definition ->
            val earnedRecord = earnedById[definition.id]
            val matchesState = when (filter) {
                EarnedFilter.ALL -> true
                EarnedFilter.EARNED -> earnedRecord != null
                EarnedFilter.LOCKED -> earnedRecord == null
            }
            matchesState && (category == null || definition.category == category)
        }
        items(visible, key = { it.id }) { definition ->
            val record = earnedById[definition.id]
            val locked = record == null
            Box(Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (locked) MaterialTheme.colorScheme.surfaceVariant else earnedGreen.copy(alpha = 0.22f)
                    )
                ) {
                    ListItem(
                        colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(if (definition.hidden && locked) "Secret achievement" else definition.title) },
                        supportingContent = {
                            Text(
                                if (definition.hidden && locked) "Keep tracking to discover this achievement."
                                else definition.description + (record?.let { "\nEarned ${DateFormat.getDateInstance().format(Date(it.earnedAt))}" } ?: definition.threshold?.let { "\nTarget: ${it.toInt()}" }.orEmpty())
                            )
                        },
                        overlineContent = { Text(definition.category.name.replace('_', ' ')) },
                        trailingContent = {
                            if (locked) Text("Locked") else Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Earned", tint = earnedGreen)
                                Text(if (!record.seen) " New" else " Earned")
                            }
                        }
                    )
                }
                if (locked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked achievement",
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp).size(52.dp).alpha(0.28f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
