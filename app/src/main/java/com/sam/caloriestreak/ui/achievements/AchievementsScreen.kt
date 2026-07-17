package com.sam.caloriestreak.ui.achievements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ListItemDefaults
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
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import java.text.DateFormat
import java.util.Date

private enum class EarnedFilter { ALL, EARNED, LOCKED }

@Composable
fun AchievementsScreen(earned: List<EarnedAchievementEntity>, onSeen: () -> Unit) {
    var filter by remember { mutableStateOf(EarnedFilter.ALL) }
    var category by remember { mutableStateOf<AchievementCategory?>(null) }
    val earnedById = earned.associateBy { it.achievementId }
    LaunchedEffect(Unit) { onSeen() }

    LazyColumn(
        contentPadding = PaddingValues(AppDimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)
    ) {
        item {
            AppSectionHeader(
                title = "Achievements",
                subtitle = "${earned.size} of ${AchievementRegistry.all.size} earned"
            )
            Row(
                modifier = Modifier.padding(top = AppDimensions.Space12),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)
            ) {
                EarnedFilter.entries.forEach { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { filter = option },
                        label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(top = AppDimensions.Space8),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.Space8)
            ) {
                FilterChip(selected = category == null, onClick = { category = null }, label = { Text("All categories") })
                AchievementCategory.entries.filterNot { it == AchievementCategory.HIDDEN }.forEach { option ->
                    FilterChip(
                        selected = category == option,
                        onClick = { category = option },
                        label = { Text(option.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }) }
                    )
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
            val accent = achievementAccent(definition.category)
            Box(Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = if (locked) MaterialTheme.colorScheme.surfaceContainer else accent.copy(alpha = 0.13f)
                    ),
                    border = BorderStroke(1.dp, if (locked) MaterialTheme.colorScheme.outlineVariant else accent.copy(alpha = 0.52f))
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = {
                            Text(
                                if (definition.hidden && locked) "Secret achievement" else definition.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = {
                            Text(
                                if (definition.hidden && locked) {
                                    "Keep tracking to discover this achievement."
                                } else {
                                    definition.description + (
                                        record?.let { "\nEarned ${DateFormat.getDateInstance().format(Date(it.earnedAt))}" }
                                            ?: definition.threshold?.let { "\nTarget: ${it.toInt()}" }.orEmpty()
                                        )
                                }
                            )
                        },
                        overlineContent = {
                            Text(
                                definition.category.name.replace('_', ' '),
                                color = if (locked) MaterialTheme.colorScheme.onSurfaceVariant else accent
                            )
                        },
                        trailingContent = {
                            if (locked) {
                                Text("Locked", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Earned", tint = AppColors.Success)
                                    Text(if (record?.seen == false) " New" else " Earned", color = AppColors.Success)
                                }
                            }
                        }
                    )
                }
                if (locked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked achievement",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = AppDimensions.Space24)
                            .size(52.dp)
                            .alpha(0.24f),
                        tint = achievementAccent(definition.category)
                    )
                }
            }
        }
    }
}

private fun achievementAccent(category: AchievementCategory): Color = when (category) {
    AchievementCategory.TIME -> AppColors.Cyan
    AchievementCategory.CALORIES -> AppColors.Coral
    AchievementCategory.SCORE -> AppColors.Achievement
    AchievementCategory.RECIPES, AchievementCategory.MEAL_HABITS -> AppColors.Violet
    AchievementCategory.STREAKS -> AppColors.Warning
    AchievementCategory.FREEZES -> AppColors.Freeze
    AchievementCategory.WEIGHT -> AppColors.Weight
    AchievementCategory.GROCERY -> AppColors.Weight
    AchievementCategory.HIDDEN -> AppColors.Locked
}
