package com.sam.caloriestreak.data.settings

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "calorie_streak_settings")

class SettingsStore(private val context: Context) {
    private val targetKey = doublePreferencesKey("daily_target")

    val dailyTarget: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[targetKey] ?: 1650.0
    }

    suspend fun setDailyTarget(value: Double) {
        context.dataStore.edit { it[targetKey] = value }
    }
}
