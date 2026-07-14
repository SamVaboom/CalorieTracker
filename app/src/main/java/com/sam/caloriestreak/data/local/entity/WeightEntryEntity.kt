package com.sam.caloriestreak.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries", indices = [Index("timestamp")])
data class WeightEntryEntity(
    @PrimaryKey val id: String,
    val kilograms: Double,
    val timestamp: Long,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
