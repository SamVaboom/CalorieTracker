package com.sam.caloriestreak.domain.achievement

import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementPopupPolicyTest {
    private fun record(
        id: String,
        earnedAt: Long,
        dismissed: Boolean = false,
        suppressed: Boolean = false
    ) = EarnedAchievementEntity(
        id = id,
        achievementId = "bullseye",
        earnedAt = earnedAt,
        triggeringEpochDay = 1,
        progressAtUnlock = 1.0,
        popupDismissed = dismissed,
        popupSuppressed = suppressed
    )

    @Test fun newlyUnlockedAchievementAppearsImmediately() {
        assertEquals(listOf("new"), AchievementPopupPolicy.orderedPending(listOf(record("new", 1))).map { it.id })
    }

    @Test fun unseenPopupStillAppearsOnNextLaunch() {
        val persisted = record("persisted", 1)
        assertTrue(AchievementPopupPolicy.orderedPending(listOf(persisted)).isNotEmpty())
    }

    @Test fun dismissedPopupDoesNotReappear() {
        assertTrue(AchievementPopupPolicy.orderedPending(listOf(record("done", 1, dismissed = true))).isEmpty())
    }

    @Test fun multipleUnlocksAreStableAndQueuedOldestFirst() {
        val ordered = AchievementPopupPolicy.orderedPending(listOf(record("third", 3), record("first", 1), record("second", 2)))
        assertEquals(listOf("first", "second", "third"), ordered.map { it.id })
    }

    @Test fun repeatedEvaluationDoesNotDuplicateQueueEntries() {
        val records = listOf(record("one", 1), record("two", 2))
        assertEquals(
            AchievementPopupPolicy.orderedPending(records).map { it.id },
            AchievementPopupPolicy.orderedPending(records).map { it.id }
        )
    }

    @Test fun dynamicAchievementCanAppearAgainWithNewUnlockOccurrence() {
        val oldDismissedOccurrence = record("old-occurrence", 1, dismissed = true)
        val newOccurrence = record("new-occurrence", 2)
        val pending = AchievementPopupPolicy.orderedPending(listOf(oldDismissedOccurrence, newOccurrence))
        assertEquals(listOf("new-occurrence"), pending.map { it.id })
    }

    @Test fun retroactiveBulkUnlockUsesOneSummary() {
        assertFalse(AchievementPopupPolicy.shouldUseRetroactiveSummary(true, 3))
        assertTrue(AchievementPopupPolicy.shouldUseRetroactiveSummary(true, 4))
        assertFalse(AchievementPopupPolicy.shouldUseRetroactiveSummary(false, 20))
    }

    @Test fun suppressedIndividualPopupsAreExcludedFromBulkQueue() {
        assertTrue(AchievementPopupPolicy.orderedPending(listOf(record("bulk", 1, suppressed = true))).isEmpty())
    }
}
