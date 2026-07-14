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
    ): StreakSnapshot = calculateInternal(
        days = days,
        initialFreezes = 0,
        initialProgress = 0,
        freezeCutoffExclusive = Long.MIN_VALUE,
        maxFreezes = maxFreezes,
        requiredDays = requiredDays
    )

    fun calculateWithBaseline(
        days: List<DailyLogEntity>,
        baseline: FreezeRuleBaseline,
        maxFreezes: Int = StreakRules.MAX_STORED_FREEZES,
        requiredDays: Int = StreakRules.FREEZE_REQUIRED_DAYS
    ): StreakSnapshot = calculateInternal(
        days = days,
        initialFreezes = baseline.freezes.coerceIn(0, maxFreezes),
        initialProgress = if (baseline.freezes >= maxFreezes) 0 else baseline.progress.coerceIn(0, requiredDays - 1),
        freezeCutoffExclusive = baseline.cutoffEpochDay,
        maxFreezes = maxFreezes,
        requiredDays = requiredDays
    )

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
        var freezes = initialFreezes.coerceIn(0, maxFreezes)
        var progress = if (freezes >= maxFreezes) 0 else initialProgress.coerceIn(0, requiredDays - 1)

        days.sortedBy { it.dateEpochDay }.forEach { day ->
            if (day.finalized) {
                if (day.streakSuccessful || day.freezeUsed) current++ else current = 0
                best = maxOf(best, current)

                if (day.dateEpochDay > freezeCutoffExclusive && day.freezeQualifying) {
                    if (freezes < maxFreezes) {
                        progress++
                        if (progress >= requiredDays) {
                            freezes++
                            progress = 0
                        }
                    } else {
                        // Do not bank invisible qualifying days while freeze storage is full.
                        progress = 0
                    }
                }
            }

            if (day.dateEpochDay > freezeCutoffExclusive && day.freezeUsed && freezes > 0) {
                freezes--
            }
        }
        return StreakSnapshot(current, best, freezes.coerceAtMost(maxFreezes), progress)
    }
}
