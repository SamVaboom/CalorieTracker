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
        return calculateInternal(
            days = days,
            initialFreezes = 0,
            initialProgress = 0,
            freezeCutoffExclusive = Long.MIN_VALUE,
            maxFreezes = maxFreezes,
            requiredDays = requiredDays
        )
    }

    fun calculateWithBaseline(
        days: List<DailyLogEntity>,
        baseline: FreezeRuleBaseline,
        maxFreezes: Int = StreakRules.MAX_STORED_FREEZES,
        requiredDays: Int = StreakRules.FREEZE_REQUIRED_DAYS
    ): StreakSnapshot {
        return calculateInternal(
            days = days,
            initialFreezes = baseline.freezes.coerceIn(0, maxFreezes),
            initialProgress = baseline.progress.coerceAtLeast(0),
            freezeCutoffExclusive = baseline.cutoffEpochDay,
            maxFreezes = maxFreezes,
            requiredDays = requiredDays
        )
    }

    private fun calculateInternal(
        days: List<DailyLogEntity>,
        initialFreezes: Int,
        initialProgress: Int,
        freezeCutoffExclusive: Long,
        maxFreezes: Int,
        requiredDays: Int
    ): StreakSnapshot {
        var current = 0
        var best = 0
        var freezes = initialFreezes
        var progress = initialProgress

        days.sortedBy { it.dateEpochDay }.forEach { day ->
            if (day.finalized) {
                if (day.streakSuccessful || day.freezeUsed) current++ else current = 0
                best = maxOf(best, current)

                if (day.dateEpochDay > freezeCutoffExclusive && day.freezeQualifying) {
                    progress++
                    while (progress >= requiredDays && freezes < maxFreezes) {
                        freezes++
                        progress -= requiredDays
                    }
                }
            }

            // Freeze usage before the migration cutoff is already included in the saved baseline.
            if (day.dateEpochDay > freezeCutoffExclusive && day.freezeUsed && freezes > 0) {
                freezes--
            }
        }
        return StreakSnapshot(current, best, freezes, progress)
    }
}
