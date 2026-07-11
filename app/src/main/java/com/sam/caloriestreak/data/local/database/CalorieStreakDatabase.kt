package com.sam.caloriestreak.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sam.caloriestreak.data.local.dao.IngredientDao
import com.sam.caloriestreak.data.local.entity.IngredientEntity

@Database(
    entities = [IngredientEntity::class],
    version = 1,
    exportSchema = true
)
abstract class CalorieStreakDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
}
