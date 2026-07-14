package com.sam.caloriestreak.domain.calculation

object FreezeTodayPolicy {
    fun canUseFreeze(availableFreezes: Int, alreadyFrozen: Boolean): Boolean =
        availableFreezes > 0 && !alreadyFrozen

    fun effectiveScore(actualScore: Double, frozen: Boolean): Double =
        if (frozen) 100.0 else actualScore.coerceIn(0.0, 100.0)

    fun qualifiesForProgress(actualScore: Double): Boolean =
        actualScore >= StreakRules.FREEZE_QUALIFYING_SCORE
}
