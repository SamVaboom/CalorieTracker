package com.sam.caloriestreak.ui

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.GroceryItemEntity
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.domain.calculation.StreakRules

data class RecipeSummary(
    val recipe: RecipeEntity,
    val items: List<RecipeItemEntity>,
    val totalCalories: Double,
    val caloriesPerServing: Double
)

data class AppUiState(
    val ingredients: List<IngredientEntity> = emptyList(),
    val allIngredients: List<IngredientEntity> = emptyList(),
    val recipes: List<RecipeSummary> = emptyList(),
    val allRecipes: List<RecipeSummary> = emptyList(),
    val meals: List<MealLogEntity> = emptyList(),
    val groceryItems: List<GroceryItemEntity> = emptyList(),
    val dailyLogs: List<DailyLogEntity> = emptyList(),
    val todayCalories: Double = 0.0,
    /** Actual calorie-curve score. Never replaced by a freeze. */
    val todayScore: Double = 0.0,
    /** Score used for the dashboard/streak presentation. 100% while Freeze Today is active. */
    val todayEffectiveScore: Double = 0.0,
    val todayFrozen: Boolean = false,
    val status: String = "Bad",
    val target: Double = 1650.0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val freezes: Int = 0,
    val freezeProgress: Int = 0,
    val freezeRequiredDays: Int = StreakRules.FREEZE_REQUIRED_DAYS
)
