package com.sam.caloriestreak.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_events",
    indices = [Index("type"), Index("epochDay"), Index("timestamp")]
)
data class ActivityEventEntity(
    @PrimaryKey val id: String,
    val type: String,
    val epochDay: Long,
    val timestamp: Long,
    val metadata: String? = null
)

object ActivityEventType {
    const val APP_OPEN = "app_open"
    const val GROCERY_GENERATED = "grocery_generated"
    const val GROCERY_COMPLETED = "grocery_completed"
    const val LAST_FREEZE_USED = "last_freeze_used"
}
