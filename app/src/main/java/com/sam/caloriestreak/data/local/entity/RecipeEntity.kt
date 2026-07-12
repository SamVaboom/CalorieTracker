package com.sam.caloriestreak.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
    val servings: Double = 1.0,
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
