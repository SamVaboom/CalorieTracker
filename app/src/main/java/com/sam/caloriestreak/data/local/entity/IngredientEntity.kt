package com.sam.caloriestreak.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val id: String,
    val name: String,
    val brand: String? = null,
    val calories: Double,
    val referenceAmount: Double,
    val referenceUnit: String,
    val category: String? = null,
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
