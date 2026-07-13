package com.sam.caloriestreak.data.settings

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sam.caloriestreak.domain.calculation.FreezeRuleBaseline
import com.sam.caloriestreak.domain.calculation.StreakSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "calorie_streak_settings")

class SettingsStore(private val context: Context) {
    private val targetKey = doublePreferencesKey("daily_target")
    private val freezeRuleCutoffKey = longPreferencesKey("freeze_rule_7_cutoff_epoch_day")
    private val freezeBaselineCountKey = intPreferencesKey("freeze_rule_7_baseline_count")
    private val freezeBaselineProgressKey = intPreferencesKey("freeze_rule_7_baseline_progress")

    val dailyTarget: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[targetKey] ?: 1650.0
    }

    val freezeRuleBaseline: Flow<FreezeRuleBaseline?> = context.dataStore.data.map { preferences ->
        val cutoff = preferences[freezeRuleCutoffKey] ?: return@map null
        FreezeRuleBaseline(
            cutoffEpochDay = cutoff,
            freezes = preferences[freezeBaselineCountKey] ?: 0,
            progress = preferences[freezeBaselineProgressKey] ?: 0
        )
    }

    suspend fun setDailyTarget(value: Double) {
        context.dataStore.edit { it[targetKey] = value }
    }

    suspend fun preserveFreezeStateForSevenDayRule(cutoffEpochDay: Long, oldSnapshot: StreakSnapshot) {
        context.dataStore.edit { preferences ->
            if (preferences[freezeRuleCutoffKey] == null) {
                preferences[freezeRuleCutoffKey] = cutoffEpochDay
                preferences[freezeBaselineCountKey] = oldSnapshot.freezes
                preferences[freezeBaselineProgressKey] = oldSnapshot.progress
            }
        }
    }
}
