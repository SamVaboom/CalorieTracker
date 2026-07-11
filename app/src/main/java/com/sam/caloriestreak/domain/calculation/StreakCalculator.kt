package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.DailyLogEntity

data class StreakSnapshot(
    val current: Int,
    val best: Int,
    val freezes: Int,
    val progress: Int
)

object StreakCalculator {
    fun calculate(days: List<DailyLogEntity>, maxFreezes: Int = 3, requiredDays: Int = 5): StreakSnapshot {
        var current = 0
        var best = 0
        var freezes = 0
        var progress = 0
        days.sortedBy { it.dateEpochDay }.forEach { day ->
            if (day.streakSuccessful || day.freezeUsed) current++ else current = 0
            best = maxOf(best, current)
            if (day.freezeQualifying) {
                progress++
                while (progress >= requiredDays && freezes < maxFreezes) {
                    freezes++
                    progress -= requiredDays
                }
            }
            if (day.freezeUsed && freezes > 0) freezes--
        }
        return StreakSnapshot(current, best, freezes, progress)
    }
}
