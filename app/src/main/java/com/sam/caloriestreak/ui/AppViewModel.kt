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
import com.sam.caloriestreak.data.settings.SettingsStore
import com.sam.caloriestreak.domain.calculation.FreezeTodayPolicy
import com.sam.caloriestreak.domain.calculation.HistoryRebuilder
import com.sam.caloriestreak.domain.calculation.IngredientCalorieCalculator
import com.sam.caloriestreak.domain.calculation.RecipeCalorieCalculator
import com.sam.caloriestreak.domain.calculation.ScoreCalculator
import com.sam.caloriestreak.domain.calculation.StreakCalculator
import com.sam.caloriestreak.domain.calculation.StreakRules
import com.sam.caloriestreak.domain.editing.IngredientDraft
import com.sam.caloriestreak.domain.editing.RecipeDraft
import com.sam.caloriestreak.domain.editing.RecipeIngredientDraft
import com.sam.caloriestreak.domain.editing.UnitConverter
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val settingsStore = SettingsStore(application)
    private val historyRebuilder = HistoryRebuilder(appDao)

    init {
        viewModelScope.launch {
            val cutoff = LocalDate.now().minusDays(1).toEpochDay()
            val existingDays = appDao.allDailyLogs().filter { it.dateEpochDay <= cutoff }
            val legacySnapshot = StreakCalculator.calculate(
                days = existingDays,
                requiredDays = StreakRules.LEGACY_FREEZE_REQUIRED_DAYS
            )
            settingsStore.preserveFreezeStateForSevenDayRule(cutoff, legacySnapshot)
            historyRebuilder.rebuild(settingsStore.dailyTarget.first())
        }
    }

    private val core = combine(
        ingredientDao.observeAll(),
        appDao.observeAllRecipes(),
        appDao.observeRecipeItems(),
        appDao.observeMeals(),
        appDao.observeGroceryItems()
    ) { ingredients, recipes, items, meals, grocery ->
        CoreData(ingredients, recipes, items, meals, grocery)
    }

    val state = combine(
        core,
        appDao.observeDailyLogs(),
        settingsStore.dailyTarget,
        settingsStore.freezeRuleBaseline
    ) { data, daily, target, freezeBaseline ->
        val scoreCalculator = ScoreCalculator.forTarget(target)
        val ingredientMap = data.ingredients.associateBy { it.id }
        val summaries = data.recipes.map { recipe ->
            val recipeItems = data.items.filter { it.recipeId == recipe.id }.map { item ->
                ingredientMap[item.ingredientId]?.let { ingredient ->
                    item.copy(ingredientName = ingredient.name)
                } ?: item
            }
            val total = recipeItems.sumOf { item ->
                val ingredient = ingredientMap[item.ingredientId]
                if (ingredient == null) 0.0 else {
                    val convertedAmount = UnitConverter.convert(
                        amount = item.amount,
                        fromUnit = item.unit,
                        toUnit = ingredient.referenceUnit
                    ) ?: 0.0
                    IngredientCalorieCalculator.calories(
                        ingredient.calories,
                        ingredient.referenceAmount,
                        convertedAmount
                    )
                }
            }
            RecipeSummary(
                recipe = recipe,
                items = recipeItems,
                totalCalories = total,
                caloriesPerServing = RecipeCalorieCalculator.perServing(total, recipe.servings)
            )
        }
        val today = LocalDate.now().toEpochDay()
        val todayCalories = data.meals.filter { it.dateEpochDay == today }.sumOf { it.calories }
        val actualScore = scoreCalculator.calculate(todayCalories)
        val todayRecord = daily.firstOrNull { it.dateEpochDay == today }
        val todayFrozen = todayRecord?.manualCheatDay == true && todayRecord.freezeUsed
        val effectiveScore = FreezeTodayPolicy.effectiveScore(actualScore, todayFrozen)
        val streak = freezeBaseline?.let { StreakCalculator.calculateWithBaseline(daily, it) }
            ?: StreakCalculator.calculate(daily)
        AppUiState(
            ingredients = data.ingredients.filterNot { it.archived },
            allIngredients = data.ingredients,
            recipes = summaries.filterNot { it.recipe.archived },
            allRecipes = summaries,
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    fun setDailyTarget(target: Int): Result<Unit> {
        if (target !in 800..5000) return Result.failure(IllegalArgumentException("Target must be between 800 and 5000 kcal"))
        viewModelScope.launch {
            settingsStore.setDailyTarget(target.toDouble())
            syncTodayOverride(target.toDouble())
        }
        return Result.success(Unit)
    }

    fun saveIngredient(existing: IngredientEntity?, draft: IngredientDraft) {
        if (!draft.isValid()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            ingredientDao.upsert(
                draft.toEntity(
                    existing = existing,
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    now = now
                )
            )
        }
    }

    fun addIngredient(name: String, calories: Double, referenceAmount: Double, unit: String) {
        saveIngredient(
            existing = null,
            draft = IngredientDraft(
                name = name,
                calories = calories,
                referenceAmount = referenceAmount,
                referenceUnit = unit
            )
        )
    }

    fun deleteIngredient(ingredient: IngredientEntity) = viewModelScope.launch {
        if (appDao.recipeUseCount(ingredient.id) > 0) {
            ingredientDao.upsert(ingredient.copy(archived = true, updatedAt = System.currentTimeMillis()))
        } else ingredientDao.delete(ingredient)
    }

    fun saveRecipe(existing: RecipeSummary?, draft: RecipeDraft) {
        val ingredients = state.value.allIngredients
        if (!draft.isValid(ingredients)) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val recipe = draft.toEntity(
                existing = existing?.recipe,
                id = existing?.recipe?.id ?: UUID.randomUUID().toString(),
                now = now
            )
            val items = draft.toItems(
                recipeId = recipe.id,
                ingredients = ingredients,
                existingItems = existing?.items.orEmpty(),
                idFactory = { UUID.randomUUID().toString() }
            )
            appDao.replaceRecipe(recipe, items)
        }
    }

    fun addRecipe(name: String, servings: Double, selected: List<Pair<IngredientEntity, Double>>) {
        saveRecipe(
            existing = null,
            draft = RecipeDraft(
                name = name,
                servings = servings,
                items = selected.filter { it.second > 0.0 }.map { (ingredient, amount) ->
                    RecipeIngredientDraft(ingredient.id, amount, ingredient.referenceUnit)
                }
            )
        )
    }

    fun logRecipe(summary: RecipeSummary, multiplier: Double, description: String) {
        if (multiplier <= 0) return
        saveMeal(
            recipeId = summary.recipe.id,
            name = summary.recipe.name,
            portion = description,
            multiplier = multiplier,
            calories = RecipeCalorieCalculator.forFraction(summary.totalCalories, multiplier)
        )
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
            syncTodayOverride(state.value.target)
        }
    }

    fun deleteMeal(meal: MealLogEntity) = viewModelScope.launch {
        appDao.deleteMeal(meal)
        syncTodayOverride(state.value.target)
        historyRebuilder.rebuild(state.value.target)
    }

    fun freezeToday() = viewModelScope.launch {
        val day = LocalDate.now().toEpochDay()
        val existing = appDao.dailyLogForDay(day)
        val alreadyFrozen = existing?.manualCheatDay == true || existing?.freezeUsed == true
        if (!FreezeTodayPolicy.canUseFreeze(state.value.freezes, alreadyFrozen)) return@launch
        val now = System.currentTimeMillis()
        val total = appDao.totalForDay(day)
        val target = state.value.target
        val actualScore = ScoreCalculator.forTarget(target).calculate(total)
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
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
                targetCalories = target,
                scoreCurveVersion = 1
            )
        )
    }

    private suspend fun syncTodayOverride(target: Double) {
        val day = LocalDate.now().toEpochDay()
        val existing = appDao.dailyLogForDay(day) ?: return
        if (existing.finalized) return
        val total = appDao.totalForDay(day)
        val score = ScoreCalculator.forTarget(target).calculate(total)
        appDao.upsertDailyLog(
            existing.copy(
                totalCalories = total,
                score = score,
                freezeQualifying = FreezeTodayPolicy.qualifiesForProgress(score),
                targetCalories = target,
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
            val existing = state.value.groceryItems.firstOrNull {
                it.ingredientId == ingredient.id && it.unit == ingredient.referenceUnit
            }
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

    fun toggleGrocery(item: GroceryItemEntity) = viewModelScope.launch {
        appDao.updateGroceryItem(item.copy(checked = !item.checked))
    }

    fun clearGrocery() = viewModelScope.launch { appDao.clearGroceryItems() }
}
