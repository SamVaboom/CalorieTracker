package com.sam.caloriestreak.ui.achievements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sam.caloriestreak.data.local.entity.AchievementPopupSummaryEntity
import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity
import com.sam.caloriestreak.domain.achievement.AchievementCategory
import com.sam.caloriestreak.domain.achievement.AchievementDefinition
import com.sam.caloriestreak.domain.achievement.AchievementRegistry
import com.sam.caloriestreak.ui.theme.AppColors
import com.sam.caloriestreak.ui.theme.AppDimensions
import com.sam.caloriestreak.ui.theme.AppMotion
import java.text.DateFormat
import java.util.Date

@Composable
fun AchievementPopupHost(
    pendingAchievements: List<EarnedAchievementEntity>,
    pendingSummary: AchievementPopupSummaryEntity?,
    onDismissAchievement: (String) -> Unit,
    onDismissSummary: (String) -> Unit,
    onOpenAchievements: () -> Unit
) {
    var locallyDismissed by remember { mutableStateOf(emptySet<String>()) }
    val summary = pendingSummary?.takeUnless { it.id in locallyDismissed }
    val achievement = if (summary == null) pendingAchievements.firstOrNull { it.id !in locallyDismissed } else null

    if (summary != null) {
        AchievementSummaryDialog(
            summary = summary,
            onClose = {
                locallyDismissed = locallyDismissed + summary.id
                onDismissSummary(summary.id)
            },
            onOpenAchievements = {
                locallyDismissed = locallyDismissed + summary.id
                onDismissSummary(summary.id)
                onOpenAchievements()
            }
        )
    } else if (achievement != null) {
        val definition = AchievementRegistry.all.firstOrNull { it.id == achievement.achievementId }
        if (definition != null) {
            AchievementUnlockedDialog(
                record = achievement,
                definition = definition,
                onClose = {
                    locallyDismissed = locallyDismissed + achievement.id
                    onDismissAchievement(achievement.id)
                }
            )
        }
    }
}

@Composable
private fun AchievementUnlockedDialog(
    record: EarnedAchievementEntity,
    definition: AchievementDefinition,
    onClose: () -> Unit
) {
    val accent = categoryAccent(definition.category)
    PopupDialogFrame(
        dialogKey = record.id,
        announcement = "Achievement unlocked. ${definition.title}",
        accent = accent,
        onClose = onClose
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(accent.copy(alpha = 0.16f), CircleShape)
                .border(1.dp, accent.copy(alpha = 0.62f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(AppDimensions.Space16))
        Text(
            text = "Achievement Unlocked",
            style = MaterialTheme.typography.labelLarge,
            color = accent,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(AppDimensions.Space8))
        Text(
            text = definition.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(AppDimensions.Space8))
        Text(
            text = definition.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(AppDimensions.Space16))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = definition.category.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge,
                color = accent
            )
            Text(
                text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(record.earnedAt)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AchievementSummaryDialog(
    summary: AchievementPopupSummaryEntity,
    onClose: () -> Unit,
    onOpenAchievements: () -> Unit
) {
    PopupDialogFrame(
        dialogKey = summary.id,
        announcement = "${summary.achievementCount} achievements unlocked from your existing history",
        accent = AppColors.Achievement,
        onClose = onClose
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(AppColors.Achievement.copy(alpha = 0.16f), CircleShape)
                .border(1.dp, AppColors.Achievement.copy(alpha = 0.62f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AppColors.Achievement, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(AppDimensions.Space16))
        Text("Achievements Unlocked", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(AppDimensions.Space8))
        Text(
            "${summary.achievementCount} achievements unlocked from your existing history.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(AppDimensions.Space20))
        Button(
            onClick = onOpenAchievements,
            modifier = Modifier.fillMaxWidth().testTag("achievement_popup_open_achievements")
        ) {
            Text("Open Achievements")
        }
    }
}

@Composable
private fun PopupDialogFrame(
    dialogKey: String,
    announcement: String,
    accent: Color,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    var entered by remember(dialogKey) { mutableStateOf(false) }
    val focusRequester = remember(dialogKey) { FocusRequester() }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(dialogKey) {
        entered = true
        withFrameNanos { }
        focusRequester.requestFocus()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("achievement_popup_scrim")
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(AppDimensions.Space16),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(AppMotion.Achievement)) + scaleIn(tween(AppMotion.Achievement), initialScale = 0.94f),
                exit = fadeOut(tween(AppMotion.Fast)) + scaleOut(tween(AppMotion.Fast), targetScale = 0.97f)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                        .heightIn(max = 620.dp)
                        .testTag("achievement_popup_card")
                        .focusRequester(focusRequester)
                        .focusable()
                        .semantics {
                            liveRegion = LiveRegionMode.Assertive
                            contentDescription = announcement
                            isTraversalGroup = true
                        },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Box {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(AppDimensions.Space24),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            content = content
                        )
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(AppDimensions.Space8)
                                .size(AppDimensions.TouchTarget)
                                .testTag("achievement_popup_close")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close achievement popup")
                        }
                    }
                }
            }
        }
    }
}

private fun categoryAccent(category: AchievementCategory): Color = when (category) {
    AchievementCategory.TIME -> AppColors.Cyan
    AchievementCategory.SCORE -> AppColors.Achievement
    AchievementCategory.CALORIES -> AppColors.Coral
    AchievementCategory.RECIPES, AchievementCategory.MEAL_HABITS -> AppColors.Violet
    AchievementCategory.GROCERY -> AppColors.Weight
    AchievementCategory.FREEZES -> AppColors.Freeze
    AchievementCategory.WEIGHT -> AppColors.Success
    AchievementCategory.HIDDEN -> AppColors.Locked
    AchievementCategory.STREAKS -> AppColors.Warning
}
