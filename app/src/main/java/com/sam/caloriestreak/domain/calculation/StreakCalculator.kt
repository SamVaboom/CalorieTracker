package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.DailyLogEntity

data class StreakSnapshot(
    val current: Int,
    val best: Int,
    val freezes: Int,
    val progress: Int
)

object StreakCalculator {
    fun calculate(
        days: List<DailyLogEntity>,
        maxFreezes: Int = StreakRules.MAX_STORED_FREEZES,
        requiredDays: Int = StreakRules.FREEZE_REQUIRED_DAYS
    ): StreakSnapshot {
        var current = 0
        var best = 0
        var freezes = 0
        var progress = 0

        days.sortedBy { it.dateEpochDay }.forEach { day ->
            if (day.finalized) {
                if (day.streakSuccessful || day.freezeUsed) current++ else current = 0
                best = maxOf(best, current)

                if (day.freezeQualifying) {
                    progress++
                    while (progress >= requiredDays && freezes < maxFreezes) {
                        freezes++
                        progress -= requiredDays
                    }
                }
            }

            // An in-progress manual Freeze Today record consumes inventory immediately but does not
            // add a streak day or freeze progress until the date is finalized.
            if (day.freezeUsed && freezes > 0) freezes--
        }
        return StreakSnapshot(current, best, freezes, progress)
    }
}
