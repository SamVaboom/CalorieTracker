package com.sam.caloriestreak.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sam.caloriestreak.data.local.database.DatabaseProvider
import com.sam.caloriestreak.data.local.entity.GroceryItemEntity
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.domain.calculation.IngredientCalorieCalculator
import com.sam.caloriestreak.domain.calculation.RecipeCalorieCalculator
import com.sam.caloriestreak.domain.calculation.ScoreCalculator
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.get(application)
    private val ingredientDao = database.ingredientDao()
    private val appDao = database.appDao()
    private val scoreCalculator = ScoreCalculator()
    private val targetFlow = MutableStateFlow(1650.0)

    val state = combine(
        ingredientDao.observeActive(),
        appDao.observeRecipes(),
        appDao.observeRecipeItems(),
        appDao.observeMeals(),
        appDao.observeGroceryItems(),
        appDao.observeDailyLogs(),
        targetFlow
    ) { ingredients, recipes, items, meals, grocery, daily, target ->
        val summaries = recipes.map { recipe ->
            val recipeItems = items.filter { it.recipeId == recipe.id }
            val total = recipeItems.sumOf { item ->
                val ingredient = ingredients.firstOrNull { it.id == item.ingredientId }
                if (ingredient == null) 0.0 else IngredientCalorieCalculator.calories(
                    ingredient.calories,
                    ingredient.referenceAmount,
                    item.amount
                )
            }
            RecipeSummary(recipe, recipeItems, total, RecipeCalorieCalculator.perServing(total, recipe.servings))
        }
        val today = LocalDate.now().toEpochDay()
        val todayCalories = meals.filter { it.dateEpochDay == today }.sumOf { it.calories }
        val score = scoreCalculator.calculate(todayCalories)
        val finalized = daily.sortedBy { it.dateEpochDay }
        var current = 0
        var best = 0
        var freezes = 0
        var progress = 0
        finalized.forEach { day ->
            if (day.streakSuccessful || day.freezeUsed) current++ else current = 0
            if (current > best) best = current
            if (day.freezeQualifying) {
                progress++
                if (progress >= 5 && freezes < 3) {
                    freezes++
                    progress -= 5
                }
            }
            if (day.freezeUsed && freezes > 0) freezes--
        }
        AppUiState(
            ingredients = ingredients,
            recipes = summaries,
            meals = meals,
            groceryItems = grocery,
            dailyLogs = daily,
            todayCalories = todayCalories,
            todayScore = score,
            status = scoreCalculator.status(score),
            target = target,
            currentStreak = current,
            bestStreak = best,
            freezes = freezes,
            freezeProgress = progress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    fun addIngredient(name: String, calories: Double, referenceAmount: Double, unit: String) {
        if (name.isBlank() || calories < 0 || referenceAmount <= 0) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            ingredientDao.upsert(IngredientEntity(UUID.randomUUID().toString(), name.trim(), calories = calories, referenceAmount = referenceAmount, referenceUnit = unit, createdAt = now, updatedAt = now))
        }
    }

    fun deleteIngredient(ingredient: IngredientEntity) = viewModelScope.launch { ingredientDao.delete(ingredient) }

    fun addRecipe(name: String, servings: Double, selected: List<Pair<IngredientEntity, Double>>) {
        if (name.isBlank() || servings <= 0 || selected.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val recipeId = UUID.randomUUID().toString()
            appDao.upsertRecipe(RecipeEntity(recipeId, name.trim(), servings = servings, createdAt = now, updatedAt = now))
            appDao.upsertRecipeItems(selected.filter { it.second > 0 }.map { (ingredient, amount) ->
                RecipeItemEntity(UUID.randomUUID().toString(), recipeId, ingredient.id, ingredient.name, amount, ingredient.referenceUnit)
            })
        }
    }

    fun logRecipe(summary: RecipeSummary, multiplier: Double, description: String) {
        if (multiplier <= 0) return
        val calories = RecipeCalorieCalculator.forFraction(summary.totalCalories, multiplier)
        saveMeal(summary.recipe.id, summary.recipe.name, description, multiplier, calories)
    }

    fun logManual(description: String, calories: Double) {
        if (description.isBlank() || calories < 0) return
        saveMeal(null, description.trim(), "Manual", 1.0, calories)
    }

    private fun saveMeal(recipeId: String?, name: String, portion: String, multiplier: Double, calories: Double) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            appDao.upsertMeal(MealLogEntity(UUID.randomUUID().toString(), LocalDate.now().toEpochDay(), now, recipeId, name, portion, multiplier, calories, createdAt = now, updatedAt = now))
        }
    }

    fun deleteMeal(meal: MealLogEntity) = viewModelScope.launch { appDao.deleteMeal(meal) }

    fun generateGrocery(summary: RecipeSummary, multiplier: Double) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newItems = summary.items.map { item ->
                GroceryItemEntity(UUID.randomUUID().toString(), item.ingredientId, item.ingredientName, item.amount * multiplier, item.unit, createdAt = now)
            }
            appDao.upsertGroceryItems(newItems)
        }
    }

    fun toggleGrocery(item: GroceryItemEntity) = viewModelScope.launch { appDao.updateGroceryItem(item.copy(checked = !item.checked)) }
    fun clearGrocery() = viewModelScope.launch { appDao.clearGroceryItems() }
}
