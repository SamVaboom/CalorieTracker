package com.sam.caloriestreak.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sam.caloriestreak.data.local.dao.AppDao
import com.sam.caloriestreak.data.local.dao.FeatureDao
import com.sam.caloriestreak.data.local.dao.IngredientDao
import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity
import com.sam.caloriestreak.data.local.entity.GroceryItemEntity
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity

@Database(
    entities = [
        IngredientEntity::class,
        RecipeEntity::class,
        RecipeItemEntity::class,
        MealLogEntity::class,
        GroceryItemEntity::class,
        DailyLogEntity::class,
        WeightEntryEntity::class,
        EarnedAchievementEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class CalorieStreakDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun appDao(): AppDao
    abstract fun featureDao(): FeatureDao
}
