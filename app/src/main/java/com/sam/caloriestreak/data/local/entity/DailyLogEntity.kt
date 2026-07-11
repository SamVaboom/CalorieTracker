package com.sam.caloriestreak.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val dateEpochDay: Long,
    val totalCalories: Double,
    val score: Double,
    val finalized: Boolean = false,
    val streakSuccessful: Boolean = false,
    val freezeUsed: Boolean = false,
    val manualCheatDay: Boolean = false,
    val freezeQualifying: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
