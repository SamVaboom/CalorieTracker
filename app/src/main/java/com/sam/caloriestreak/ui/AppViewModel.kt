package com.sam.caloriestreak.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sam.caloriestreak.data.local.database.DatabaseProvider
import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.GroceryItemEntity
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.domain.calculation.FreezeTodayPolicy
import com.sam.caloriestreak.domain.calculation.HistoryRebuilder
import com.sam.caloriestreak.domain.calculation.IngredientCalorieCalculator
import com.sam.caloriestreak.domain.calculation.RecipeCalorieCalculator
import com.sam.caloriestreak.domain.calculation.ScoreCalculator
import com.sam.caloriestreak.domain.calculation.StreakCalculator
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class CoreData(
    val ingredients: List<IngredientEntity>,
    val recipes: List<RecipeEntity>,
    val items: List<RecipeItemEntity>,
    val meals: List<MealLogEntity>,
    val grocery: List<GroceryItemEntity>
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.get(application)
    private val ingredientDao = database.ingredientDao()
    private val appDao = database.appDao()
    private val historyRebuilder = HistoryRebuilder(appDao)
    private val preferences = application.getSharedPreferences("calorie_streak_settings", Application.MODE_PRIVATE)
    private val targetFlow = MutableStateFlow(preferences.getInt("daily_calorie_target", ScoreCalculator.DEFAULT_TARGET.toInt()).toDouble())

    init {
        viewModelScope.launch { historyRebuilder.rebuild(targetFlow.value) }
    }

    private val core = combine(
        ingredientDao.observeAll(),
        appDao.observeRecipes(),
        appDao.observeRecipeItems(),
        appDao.observeMeals(),
        appDao.observeGroceryItems()
    ) { ingredients, recipes, items, meals, grocery ->
        CoreData(ingredients, recipes, items, meals, grocery)
    }

    val state = combine(core, appDao.observeDailyLogs(), targetFlow) { data, daily, target ->
        val scoreCalculator = ScoreCalculator.forTarget(target)
        val summaries = data.recipes.map { recipe ->
            val recipeItems = data.items.filter { it.recipeId == recipe.id }
            val total = recipeItems.sumOf { item ->
                val ingredient = data.ingredients.firstOrNull { it.id == item.ingredientId }
                if (ingredient == null) 0.0 else IngredientCalorieCalculator.calories(
                    ingredient.calories,
                    ingredient.referenceAmount,
                    item.amount
                )
            }
            RecipeSummary(recipe, recipeItems, total, RecipeCalorieCalculator.perServing(total, recipe.servings))
        }
        val today = LocalDate.now().toEpochDay()
        val todayCalories = data.meals.filter { it.dateEpochDay == today }.sumOf { it.calories }
        val actualScore = scoreCalculator.calculate(todayCalories)
        val todayRecord = daily.firstOrNull { it.dateEpochDay == today }
        val todayFrozen = todayRecord?.manualCheatDay == true && todayRecord.freezeUsed
        val effectiveScore = FreezeTodayPolicy.effectiveScore(actualScore, todayFrozen)
        val streak = StreakCalculator.calculate(daily)
        AppUiState(
            ingredients = data.ingredients.filterNot { it.archived },
            recipes = summaries,
            meals = data.meals,
            groceryItems = data.grocery,
            dailyLogs = daily,
            todayCalories = todayCalories,
            todayScore = actualScore,
            todayEffectiveScore = effectiveScore,
            todayFrozen = todayFrozen,
            status = if (todayFrozen) "Freeze active" else scoreCalculator.status(actualScore),
            target = target,
            currentStreak = streak.current,
            bestStreak = streak.best,
            freezes = streak.freezes,
            freezeProgress = streak.progress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState(target = targetFlow.value))

    fun setTarget(target: Int): Result<Unit> {
        if (target !in 800..5000) return Result.failure(IllegalArgumentException("Target must be between 800 and 5000 kcal"))
        preferences.edit().putInt("daily_calorie_target", target).apply()
        targetFlow.value = target.toDouble()
        viewModelScope.launch { syncTodayOverride() }
        return Result.success(Unit)
    }

    fun addIngredient(name: String, calories: Double, referenceAmount: Double, unit: String) {
        if (name.isBlank() || calories < 0 || referenceAmount <= 0 || unit.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            ingredientDao.upsert(
                IngredientEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    calories = calories,
                    referenceAmount = referenceAmount,
                    referenceUnit = unit.trim(),
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun deleteIngredient(ingredient: IngredientEntity) = viewModelScope.launch {
        val isUsed = state.value.recipes.any { summary -> summary.items.any { it.ingredientId == ingredient.id } }
        if (isUsed) ingredientDao.upsert(ingredient.copy(archived = true, updatedAt = System.currentTimeMillis())) else ingredientDao.delete(ingredient)
    }

    fun addRecipe(name: String, servings: Double, selected: List<Pair<IngredientEntity, Double>>) {
        if (name.isBlank() || servings <= 0 || selected.none { it.second > 0 }) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val recipeId = UUID.randomUUID().toString()
            appDao.upsertRecipe(RecipeEntity(recipeId, name.trim(), servings = servings, createdAt = now, updatedAt = now))
            appDao.upsertRecipeItems(
                selected.filter { it.second > 0 }.map { (ingredient, amount) ->
                    RecipeItemEntity(UUID.randomUUID().toString(), recipeId, ingredient.id, ingredient.name, amount, ingredient.referenceUnit)
                }
            )
        }
    }

    fun logRecipe(summary: RecipeSummary, multiplier: Double, description: String) {
        if (multiplier <= 0) return
        saveMeal(summary.recipe.id, summary.recipe.name, description, multiplier, RecipeCalorieCalculator.forFraction(summary.totalCalories, multiplier))
    }

    fun logManual(description: String, calories: Double) {
        if (description.isBlank() || calories < 0) return
        saveMeal(null, description.trim(), "Manual", 1.0, calories)
    }

    private fun saveMeal(recipeId: String?, name: String, portion: String, multiplier: Double, calories: Double) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            appDao.upsertMeal(
                MealLogEntity(
                    id = UUID.randomUUID().toString(),
                    dateEpochDay = LocalDate.now().toEpochDay(),
                    timeMillis = now,
                    recipeId = recipeId,
                    recipeName = name,
                    portionDescription = portion,
                    portionMultiplier = multiplier,
                    calories = calories,
                    createdAt = now,
                    updatedAt = now
                )
            )
            syncTodayOverride()
        }
    }

    fun deleteMeal(meal: MealLogEntity) = viewModelScope.launch {
        appDao.deleteMeal(meal)
        syncTodayOverride()
        historyRebuilder.rebuild(targetFlow.value)
    }

    fun freezeToday() = viewModelScope.launch {
        val day = LocalDate.now().toEpochDay()
        val existing = appDao.dailyLogForDay(day)
        val alreadyFrozen = existing?.manualCheatDay == true || existing?.freezeUsed == true
        if (!FreezeTodayPolicy.canUseFreeze(state.value.freezes, alreadyFrozen)) return@launch
        val now = System.currentTimeMillis()
        val total = appDao.totalForDay(day)
        val actualScore = ScoreCalculator.forTarget(targetFlow.value).calculate(total)
        appDao.upsertDailyLog(
            DailyLogEntity(
                dateEpochDay = day,
                totalCalories = total,
                score = actualScore,
                finalized = false,
                streakSuccessful = false,
                freezeUsed = true,
                manualCheatDay = true,
                freezeQualifying = FreezeTodayPolicy.qualifiesForProgress(actualScore),
                targetCalories = targetFlow.value,
                scoreCurveVersion = 1,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
    }

    private suspend fun syncTodayOverride() {
        val day = LocalDate.now().toEpochDay()
        val existing = appDao.dailyLogForDay(day) ?: return
        if (existing.finalized) return
        val total = appDao.totalForDay(day)
        val score = ScoreCalculator.forTarget(targetFlow.value).calculate(total)
        appDao.upsertDailyLog(
            existing.copy(
                totalCalories = total,
                score = score,
                freezeQualifying = FreezeTodayPolicy.qualifiesForProgress(score),
                targetCalories = targetFlow.value,
                scoreCurveVersion = 1,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun generateGrocery(summary: RecipeSummary, multiplier: Double) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = state.value.groceryItems
            val additions = summary.items.map { item ->
                val match = existing.firstOrNull { it.ingredientId == item.ingredientId && it.unit == item.unit }
                GroceryItemEntity(
                    id = match?.id ?: UUID.randomUUID().toString(),
                    ingredientId = item.ingredientId,
                    name = item.ingredientName,
                    amount = (match?.amount ?: 0.0) + item.amount * multiplier,
                    unit = item.unit,
                    checked = match?.checked ?: false,
                    createdAt = match?.createdAt ?: now
                )
            }
            appDao.upsertGroceryItems(additions)
        }
    }

    fun addIngredientToGrocery(ingredient: IngredientEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = state.value.groceryItems.firstOrNull { it.ingredientId == ingredient.id && it.unit == ingredient.referenceUnit }
            appDao.upsertGroceryItems(
                listOf(
                    GroceryItemEntity(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        ingredientId = ingredient.id,
                        name = ingredient.name,
                        amount = (existing?.amount ?: 0.0) + ingredient.referenceAmount,
                        unit = ingredient.referenceUnit,
                        checked = existing?.checked ?: false,
                        createdAt = existing?.createdAt ?: now
                    )
                )
            )
        }
    }

    fun toggleGrocery(item: GroceryItemEntity) = viewModelScope.launch { appDao.updateGroceryItem(item.copy(checked = !item.checked)) }
    fun clearGrocery() = viewModelScope.launch { appDao.clearGroceryItems() }
}
