package com.sam.caloriestreak.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs", indices = [Index("dateEpochDay")])
data class MealLogEntity(
    @PrimaryKey val id: String,
    val dateEpochDay: Long,
    val timeMillis: Long,
    val recipeId: String? = null,
    val recipeName: String,
    val portionDescription: String,
    val portionMultiplier: Double,
    val calories: Double,
    /** Historical known protein snapshot. Null means unknown, while 0.0 is explicitly zero. */
    val proteinGramsSnapshot: Double? = null,
    @ColumnInfo(defaultValue = "0") val proteinDataComplete: Boolean = false,
    @ColumnInfo(defaultValue = "0") val missingProteinItemCount: Int = 0,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
