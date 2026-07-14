package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.dao.AppDao
import java.time.LocalDate

class HistoryRebuilder(private val dao: AppDao) {
    suspend fun rebuild(configuredTarget: Double = ScoreCalculator.DEFAULT_TARGET) {
        val meals = dao.allMeals()
        val existing = dao.allDailyLogs()
        val rebuilt = DailyHistoryCalculator.rebuildCompletedDays(
            meals = meals,
            existing = existing,
            todayEpochDay = LocalDate.now().toEpochDay(),
            configuredTarget = configuredTarget
        )
        dao.deleteFinalizedDailyLogs()
        rebuilt.forEach { dao.upsertDailyLog(it) }
    }
}
