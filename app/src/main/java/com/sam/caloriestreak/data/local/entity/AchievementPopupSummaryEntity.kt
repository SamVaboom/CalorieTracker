package com.sam.caloriestreak.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "achievement_popup_summaries",
    indices = [Index("createdAt")]
)
data class AchievementPopupSummaryEntity(
    @PrimaryKey val id: String,
    val achievementCount: Int,
    val createdAt: Long,
    val dismissed: Boolean = false
)
