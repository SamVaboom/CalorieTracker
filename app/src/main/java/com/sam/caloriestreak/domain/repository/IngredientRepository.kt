package com.sam.caloriestreak.domain.repository

import com.sam.caloriestreak.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun observeIngredients(): Flow<List<Ingredient>>
    suspend fun save(ingredient: Ingredient)
    suspend fun archive(id: String)
    suspend fun delete(id: String)

    // TODO: Add a cloud-backed implementation later without changing the UI layer.
}
