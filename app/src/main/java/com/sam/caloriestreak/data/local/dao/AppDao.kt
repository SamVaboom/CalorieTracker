package com.sam.caloriestreak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.GroceryItemEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM recipes WHERE archived = 0 ORDER BY favorite DESC, name")
    fun observeRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes")
    suspend fun allRecipes(): List<RecipeEntity>

    @Query("SELECT * FROM recipe_items ORDER BY ingredientName")
    fun observeRecipeItems(): Flow<List<RecipeItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipeItems(items: List<RecipeItemEntity>)

    @Query("DELETE FROM recipe_items WHERE recipeId = :recipeId")
    suspend fun deleteRecipeItems(recipeId: String)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("SELECT * FROM meal_logs ORDER BY dateEpochDay DESC, timeMillis DESC")
    fun observeMeals(): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs ORDER BY dateEpochDay, timeMillis")
    suspend fun allMeals(): List<MealLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeal(meal: MealLogEntity)

    @Delete
    suspend fun deleteMeal(meal: MealLogEntity)

    @Query("SELECT COALESCE(SUM(calories), 0) FROM meal_logs WHERE dateEpochDay = :day")
    suspend fun totalForDay(day: Long): Double

    @Query("SELECT * FROM grocery_items ORDER BY checked, name")
    fun observeGroceryItems(): Flow<List<GroceryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGroceryItems(items: List<GroceryItemEntity>)

    @Update
    suspend fun updateGroceryItem(item: GroceryItemEntity)

    @Delete
    suspend fun deleteGroceryItem(item: GroceryItemEntity)

    @Query("DELETE FROM grocery_items")
    suspend fun clearGroceryItems()

    @Query("SELECT * FROM daily_logs ORDER BY dateEpochDay DESC")
    fun observeDailyLogs(): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE dateEpochDay = :day LIMIT 1")
    suspend fun dailyLogForDay(day: Long): DailyLogEntity?

    @Query("SELECT * FROM daily_logs ORDER BY dateEpochDay")
    suspend fun allDailyLogs(): List<DailyLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyLog(log: DailyLogEntity)

    @Query("DELETE FROM daily_logs WHERE finalized = 1")
    suspend fun deleteFinalizedDailyLogs()

    @Query("DELETE FROM daily_logs WHERE dateEpochDay >= :day")
    suspend fun deleteDailyLogsFrom(day: Long)
}
