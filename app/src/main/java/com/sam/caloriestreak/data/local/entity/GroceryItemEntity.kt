package com.sam.caloriestreak.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey val id: String,
    val ingredientId: String? = null,
    val name: String,
    val amount: Double,
    val unit: String,
    val checked: Boolean = false,
    val manuallyAdded: Boolean = false,
    val createdAt: Long
)
