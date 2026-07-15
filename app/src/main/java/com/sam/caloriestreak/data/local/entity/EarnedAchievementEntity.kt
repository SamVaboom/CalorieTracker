package com.sam.caloriestreak.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "earned_achievements",
    indices = [Index(value = ["achievementId"], unique = true), Index("earnedAt")]
)
data class EarnedAchievementEntity(
    @PrimaryKey val id: String,
    val achievementId: String,
    val earnedAt: Long,
    val triggeringEpochDay: Long?,
    val progressAtUnlock: Double?,
    val seen: Boolean = false,
    val popupDismissed: Boolean = false,
    val popupSuppressed: Boolean = false,
    val unlockSource: String = UnlockSource.LIVE
)

object UnlockSource {
    const val LIVE = "LIVE"
    const val RECONCILIATION = "RECONCILIATION"
    const val LEGACY = "LEGACY"
}
