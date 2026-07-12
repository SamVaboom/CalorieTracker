package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.dao.AppDao
import java.time.LocalDate

class HistoryRebuilder(private val dao: AppDao) {
    suspend fun rebuild() {
        val meals = dao.allMeals()
        val existing = dao.allDailyLogs()
        val rebuilt = DailyHistoryCalculator.rebuildCompletedDays(
            meals = meals,
            existing = existing,
            todayEpochDay = LocalDate.now().toEpochDay()
        )

        // Replace only finalized summaries. An in-progress record for today, such as a manually
        // frozen day, must not be removed while historical totals are rebuilt.
        dao.deleteFinalizedDailyLogs()
        rebuilt.forEach { dao.upsertDailyLog(it) }
    }
}