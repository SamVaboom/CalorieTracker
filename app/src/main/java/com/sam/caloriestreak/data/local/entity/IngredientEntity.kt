package com.sam.caloriestreak.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** Retained for non-destructive compatibility with existing databases; no longer edited or displayed. */
    val brand: String? = null,
    val calories: Double,
    val referenceAmount: Double,
    val referenceUnit: String,
    /** Grams of protein for the same referenceAmount/referenceUnit. Null means not assigned. */
    val proteinPerReferenceAmount: Double? = null,
    val category: String? = null,
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
