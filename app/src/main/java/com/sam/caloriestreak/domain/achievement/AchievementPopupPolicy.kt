package com.sam.caloriestreak.domain.achievement

import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity

object AchievementPopupPolicy {
    const val RETROACTIVE_SUMMARY_THRESHOLD = 4

    fun shouldUseRetroactiveSummary(isInitialReconciliation: Boolean, newUnlockCount: Int): Boolean =
        isInitialReconciliation && newUnlockCount >= RETROACTIVE_SUMMARY_THRESHOLD

    fun orderedPending(records: List<EarnedAchievementEntity>): List<EarnedAchievementEntity> =
        records
            .asSequence()
            .filterNot { it.popupDismissed || it.popupSuppressed }
            .sortedWith(compareBy<EarnedAchievementEntity> { it.earnedAt }.thenBy { it.id })
            .toList()
}
