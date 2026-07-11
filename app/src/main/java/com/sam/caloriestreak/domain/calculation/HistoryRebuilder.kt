package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.dao.AppDao
import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import java.time.LocalDate

class HistoryRebuilder(private val dao: AppDao) {
    suspend fun rebuild() {
        val meals = dao.allMeals()
        if (meals.isEmpty()) return
        val firstDay = meals.minOf { it.dateEpochDay }
        val lastCompletedDay = LocalDate.now().minusDays(1).toEpochDay()
        if (firstDay > lastCompletedDay) return
        dao.deleteDailyLogsFrom(firstDay)
        val rebuilt = mutableListOf<DailyLogEntity>()
        for (day in firstDay..lastCompletedDay) {
            val total = meals.filter { it.dateEpochDay == day }.sumOf { it.calories }
            val finalized = DailyFinalizer.finalizeDay(day, total, rebuilt)
            dao.upsertDailyLog(finalized)
            rebuilt += finalized
        }
    }
}
