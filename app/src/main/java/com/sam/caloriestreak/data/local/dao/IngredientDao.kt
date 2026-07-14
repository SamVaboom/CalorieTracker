package com.sam.caloriestreak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY favorite DESC, name ASC")
    fun observeAll(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients")
    suspend fun all(): List<IngredientEntity>

    @Query("SELECT * FROM ingredients WHERE archived = 0 ORDER BY favorite DESC, name ASC")
    fun observeActive(): Flow<List<IngredientEntity>>

    @Upsert
    suspend fun upsert(ingredient: IngredientEntity)

    @Delete
    suspend fun delete(ingredient: IngredientEntity)
}
