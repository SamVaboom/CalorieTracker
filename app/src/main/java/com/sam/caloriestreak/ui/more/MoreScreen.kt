package com.sam.caloriestreak.ui.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions

@Composable
fun MoreScreen(
    latestWeight: Double?,
    earnedCount: Int,
    totalCount: Int,
    unseenCount: Int,
    onGrocery: () -> Unit,
    onWeight: () -> Unit,
    onProteinCorrections: () -> Unit,
    onAchievements: () -> Unit,
    onSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppDimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)
    ) {
        item { AppSectionHeader("More", subtitle = "Tools, progress and preferences") }
        item {
            MoreRow(
                title = "Grocery Lists",
                subtitle = "Build and check shopping lists",
                icon = Icons.Outlined.LocalGroceryStore,
                accent = AppColors.Cyan,
                onClick = onGrocery
            )
        }
        item {
            MoreRow(
                title = "Weight",
                subtitle = latestWeight?.let { "Latest: %.1f kg".format(it) } ?: "No weight recorded",
                icon = Icons.Outlined.MonitorWeight,
                accent = AppColors.Weight,
                onClick = onWeight
            )
        }
        item {
            MoreRow(
                title = "Protein Corrections",
                subtitle = "Explicitly correct historical meal protein",
                icon = Icons.Outlined.EditNote,
                accent = AppColors.Cyan,
                onClick = onProteinCorrections
            )
        }
        item {
            MoreRow(
                title = "Achievements",
                subtitle = "$earnedCount of $totalCount earned${if (unseenCount > 0) " · $unseenCount new" else ""}",
                icon = Icons.Outlined.EmojiEvents,
                accent = AppColors.Achievement,
                onClick = onAchievements
            )
        }
        item {
            MoreRow(
                title = "Settings",
                subtitle = "Goals, scoring and freeze rules",
                icon = Icons.Outlined.Settings,
                accent = AppColors.Violet,
                onClick = onSettings
            )
        }
    }
}

@Composable
private fun MoreRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(accent.copy(alpha = 0.15f), MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent)
                }
            },
            headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        )
    }
}
