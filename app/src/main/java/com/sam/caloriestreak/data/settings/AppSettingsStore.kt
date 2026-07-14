package com.sam.caloriestreak.data.settings

import android.content.Context
import com.sam.caloriestreak.domain.calculation.ScoreCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("calorie_streak_settings", Context.MODE_PRIVATE)
    private val _target = MutableStateFlow(preferences.getInt(KEY_TARGET, ScoreCalculator.DEFAULT_TARGET.toInt()).toDouble())
    val target: StateFlow<Double> = _target

    fun setTarget(value: Int): Result<Unit> {
        if (value !in 800..5000) return Result.failure(IllegalArgumentException("Target must be between 800 and 5000 kcal"))
        preferences.edit().putInt(KEY_TARGET, value).apply()
        _target.value = value.toDouble()
        return Result.success(Unit)
    }

    companion object {
        private const val KEY_TARGET = "daily_calorie_target"
    }
}
