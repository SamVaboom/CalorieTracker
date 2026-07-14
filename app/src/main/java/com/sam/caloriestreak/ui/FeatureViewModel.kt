package com.sam.caloriestreak.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sam.caloriestreak.data.local.database.DatabaseProvider
import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import com.sam.caloriestreak.data.settings.AppSettingsStore
import com.sam.caloriestreak.domain.achievement.AchievementEvaluator
import com.sam.caloriestreak.domain.achievement.AchievementRegistry
import com.sam.caloriestreak.domain.calculation.StreakCalculator
import com.sam.caloriestreak.domain.weight.WeightStatistics
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FeatureUiState(
    val target: Double = 1650.0,
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
    private val settings = AppSettingsStore(application)

    val state = combine(settings.target, dao.observeWeights(), dao.observeEarnedAchievements()) { target, weights, earned ->
        FeatureUiState(
            target = target,
            weights = weights,
            weightStats = WeightStatistics.calculate(weights),
            earned = earned,
            unseenCount = earned.count { !it.seen }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeatureUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                appDao.observeMeals(),
                appDao.observeDailyLogs(),
                appDao.observeRecipes(),
                ingredientDao.observeAll(),
                dao.observeWeights()
            ) { _, _, _, _, _ -> Unit }.collectLatest { reconcileAchievementsInternal() }
        }
    }

    fun setTarget(value: Int): Result<Unit> = settings.setTarget(value)

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
    fun reconcileAchievements() = viewModelScope.launch(Dispatchers.IO) { reconcileAchievementsInternal() }

    private suspend fun reconcileAchievementsInternal() {
        val meals = appDao.allMeals()
        val daily = appDao.allDailyLogs()
        val weights = dao.allWeights()
        val recipes = appDao.allRecipes()
        val ingredients = ingredientDao.all()
        val currentFreezes = StreakCalculator.calculate(daily).freezes
        val earnedIds = dao.earnedAchievementIds().toMutableSet()
        val eligibleIds = buildSet {
            addAll(AchievementEvaluator.eligibleTimeAchievements(meals).map { it.id })
            addAll(AchievementEvaluator.eligibleWeightAchievements(weights).map { it.id })
            addAll(AchievementEvaluator.eligibleCalorieAndScoreAchievements(daily))
            addAll(AchievementEvaluator.eligibleMealAndRecipeAchievements(meals, recipes.size, ingredients.size))
            addAll(AchievementEvaluator.eligibleFreezeAchievements(daily, currentFreezes))
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
