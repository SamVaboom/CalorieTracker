package com.sam.caloriestreak.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sam.caloriestreak.data.local.database.DatabaseProvider
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.domain.protein.RecipeProteinCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MealProteinCorrectionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.get(application)
    private val appDao = database.appDao()
    private val ingredientDao = database.ingredientDao()

    fun setManualProtein(meal: MealLogEntity, proteinGrams: Double?): Result<Unit> {
        if (meal.recipeId != null) return Result.failure(IllegalArgumentException("Recipe meals must be recalculated from their recipe"))
        if (proteinGrams != null && proteinGrams < 0.0) return Result.failure(IllegalArgumentException("Protein cannot be negative"))
        viewModelScope.launch(Dispatchers.IO) {
            appDao.upsertMeal(
                meal.copy(
                    proteinGramsSnapshot = proteinGrams,
                    proteinDataComplete = proteinGrams != null,
                    missingProteinItemCount = if (proteinGrams == null) 1 else 0,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return Result.success(Unit)
    }

    fun recalculateRecipeProtein(meal: MealLogEntity): Result<Unit> {
        val recipeId = meal.recipeId ?: return Result.failure(IllegalArgumentException("This meal is not linked to a saved recipe"))
        viewModelScope.launch(Dispatchers.IO) {
            val recipe = appDao.allRecipes().firstOrNull { it.id == recipeId } ?: return@launch
            val ingredients = ingredientDao.all().associateBy { it.id }
            val items = appDao.allRecipeItems().filter { it.recipeId == recipeId }
            val recipeProtein = RecipeProteinCalculator.calculate(items, ingredients)
            val hasKnownData = recipeProtein.ingredients.any { it.knownGrams != null }
            appDao.upsertMeal(
                meal.copy(
                    // Calories are deliberately preserved from the original historical snapshot.
                    proteinGramsSnapshot = (recipeProtein.knownGrams * meal.portionMultiplier).takeIf { hasKnownData },
                    proteinDataComplete = recipeProtein.complete,
                    missingProteinItemCount = recipeProtein.missingIngredientCount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return Result.success(Unit)
    }
}
