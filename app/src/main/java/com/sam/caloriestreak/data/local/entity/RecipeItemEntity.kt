package com.sam.caloriestreak.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_items",
    indices = [Index("recipeId"), Index("ingredientId")]
)
data class RecipeItemEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    val ingredientId: String,
    val ingredientName: String,
    val amount: Double,
    val unit: String,
    val note: String? = null
)
