package com.sam.caloriestreak.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sam.caloriestreak.data.local.database.DatabaseProvider
import com.sam.caloriestreak.data.local.entity.ActivityEventEntity
import com.sam.caloriestreak.data.local.entity.ActivityEventType
import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity
import com.sam.caloriestreak.data.local.entity.GroceryItemEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import com.sam.caloriestreak.data.settings.SettingsStore
import com.sam.caloriestreak.domain.achievement.AchievementEvaluator
import com.sam.caloriestreak.domain.achievement.AchievementRegistry
import com.sam.caloriestreak.domain.calculation.StreakCalculator
import com.sam.caloriestreak.domain.weight.WeightStatistics
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FeatureUiState(
    val target: Double = 1650.0,
    val weightGoal: Double? = null,
    val weights: List<WeightEntryEntity> = emptyList(),
    val weightStats: com.sam.caloriestreak.domain.weight.WeightStats = com.sam.caloriestreak.domain.weight.WeightStats(),
    val earned: List<EarnedAchievementEntity> = emptyList(),
    val unseenCount: Int = 0,
    val totalAchievements: Int = AchievementRegistry.all.size
)

class FeatureViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.get(application)
    private val dao = database.featureDao()
    private val appDao = database.appDao()
    private val ingredientDao = database.ingredientDao()
    private val settings = SettingsStore(application)

    val state = combine(
        settings.dailyTarget,
        settings.weightGoal,
        dao.observeWeights(),
        dao.observeEarnedAchievements()
    ) { target, weightGoal, weights, earned ->
        FeatureUiState(
            target = target,
            weightGoal = weightGoal,
            weights = weights,
            weightStats = WeightStatistics.calculate(weights),
            earned = earned.filter { record -> AchievementRegistry.all.any { it.id == record.achievementId } },
            unseenCount = earned.count { !it.seen && AchievementRegistry.all.any { definition -> definition.id == it.achievementId } }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeatureUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val day = LocalDate.now().toEpochDay()
            dao.insertActivityEvent(ActivityEventEntity("app_open_$day", ActivityEventType.APP_OPEN, day, System.currentTimeMillis()))
        }
        viewModelScope.launch(Dispatchers.IO) {
            val dataChanges = combine(
                appDao.observeMeals(),
                appDao.observeDailyLogs(),
                appDao.observeAllRecipes(),
                ingredientDao.observeAll(),
                dao.observeWeights()
            ) { _, _, _, _, _ -> Unit }
            combine(dataChanges, dao.observeActivityEvents(), settings.weightGoal) { _, _, _ -> Unit }
                .collectLatest { reconcileAchievementsInternal() }
        }
    }

    fun setGoals(calorieTarget: Int, weightGoal: Double?): Result<Unit> {
        if (calorieTarget !in 800..5000) return Result.failure(IllegalArgumentException("Calorie target must be between 800 and 5000 kcal"))
        if (weightGoal != null && weightGoal !in 20.0..500.0) return Result.failure(IllegalArgumentException("Weight goal must be between 20 and 500 kg"))
        viewModelScope.launch(Dispatchers.IO) {
            settings.setDailyTarget(calorieTarget.toDouble())
            settings.setWeightGoal(weightGoal)
        }
        return Result.success(Unit)
    }

    fun addWeight(kilograms: Double, timestamp: Long, note: String?): Result<Unit> {
        if (kilograms !in 20.0..500.0) return Result.failure(IllegalArgumentException("Weight must be between 20 and 500 kg"))
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            dao.upsertWeight(WeightEntryEntity(UUID.randomUUID().toString(), kilograms, timestamp, note?.trim()?.takeIf { it.isNotEmpty() }, now, now))
        }
        return Result.success(Unit)
    }

    fun updateWeight(entry: WeightEntryEntity, kilograms: Double, timestamp: Long, note: String?): Result<Unit> {
        if (kilograms !in 20.0..500.0) return Result.failure(IllegalArgumentException("Weight must be between 20 and 500 kg"))
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateWeight(entry.copy(kilograms = kilograms, timestamp = timestamp, note = note?.trim()?.takeIf { it.isNotEmpty() }, updatedAt = System.currentTimeMillis()))
        }
        return Result.success(Unit)
    }

    fun deleteWeight(entry: WeightEntryEntity) = viewModelScope.launch(Dispatchers.IO) { dao.deleteWeight(entry) }
    fun markAchievementsSeen() = viewModelScope.launch(Dispatchers.IO) { dao.markAllAchievementsSeen() }

    fun recordGroceryGenerated() = recordEvent(ActivityEventType.GROCERY_GENERATED)

    fun recordGroceryToggle(items: List<GroceryItemEntity>, toggled: GroceryItemEntity) {
        val resulting = items.map { if (it.id == toggled.id) it.copy(checked = !it.checked) else it }
        if (resulting.isNotEmpty() && resulting.all { it.checked }) recordEvent(ActivityEventType.GROCERY_COMPLETED)
    }

    fun recordLastFreezeUsed() = recordEvent(ActivityEventType.LAST_FREEZE_USED)

    private fun recordEvent(type: String) = viewModelScope.launch(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        dao.insertActivityEvent(ActivityEventEntity(UUID.randomUUID().toString(), type, LocalDate.now().toEpochDay(), now))
    }

    private suspend fun reconcileAchievementsInternal() {
        val meals = appDao.allMeals()
        val daily = appDao.allDailyLogs()
        val weights = dao.allWeights()
        val events = dao.allActivityEvents()
        val recipes = appDao.allRecipes()
        val ingredients = ingredientDao.all()
        val weightGoal = settings.weightGoal.first()
        val currentFreezes = StreakCalculator.calculate(daily).freezes
        val earnedIds = dao.earnedAchievementIds().toMutableSet()
        val registryIds = AchievementRegistry.all.map { it.id }.toSet()

        earnedIds.filterNot { it in registryIds }.toList().forEach {
            dao.deleteEarnedAchievement(it)
            earnedIds.remove(it)
        }

        val eligibleWeightIds = AchievementEvaluator.eligibleWeightAchievements(weights, weightGoal)
        val eligibleIds = buildSet {
            addAll(AchievementEvaluator.eligibleTimeAchievements(meals).map { it.id })
            addAll(eligibleWeightIds)
            addAll(AchievementEvaluator.eligibleCalorieAndScoreAchievements(daily))
            addAll(AchievementEvaluator.eligibleMealAndRecipeAchievements(meals, recipes.size, ingredients.size))
            addAll(AchievementEvaluator.eligibleFreezeAchievements(daily, currentFreezes, events))
            addAll(AchievementEvaluator.eligibleActivityAchievements(events))
        }

        AchievementRegistry.revocableWeightIds.filter { it in earnedIds && it !in eligibleWeightIds }.forEach {
            dao.deleteEarnedAchievement(it)
            earnedIds.remove(it)
        }

        AchievementRegistry.all.filter { it.id in eligibleIds && it.id !in earnedIds }.forEach { definition ->
            dao.insertEarnedAchievement(
                EarnedAchievementEntity(
                    id = UUID.randomUUID().toString(),
                    achievementId = definition.id,
                    earnedAt = System.currentTimeMillis(),
                    triggeringEpochDay = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay(),
                    progressAtUnlock = definition.threshold,
                    seen = false
                )
            )
            earnedIds += definition.id
        }
    }
}
