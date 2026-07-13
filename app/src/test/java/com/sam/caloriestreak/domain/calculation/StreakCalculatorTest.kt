package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {
    private fun day(
        index: Long,
        score: Double,
        success: Boolean = score >= StreakRules.STREAK_SCORE_THRESHOLD,
        freeze: Boolean = false
    ) = DailyLogEntity(
        dateEpochDay = index,
        totalCalories = 1650.0,
        score = score,
        finalized = true,
        streakSuccessful = success,
        freezeUsed = freeze,
        manualCheatDay = false,
        freezeQualifying = score >= StreakRules.FREEZE_QUALIFYING_SCORE,
        createdAt = 0,
        updatedAt = 0
    )

    @Test
    fun sixQualifyingDaysDoNotEarnFreeze() {
        val result = StreakCalculator.calculate((1L..6L).map { day(it, 90.0) })
        assertEquals(0, result.freezes)
        assertEquals(6, result.progress)
    }

    @Test
    fun sevenQualifyingDaysEarnOneFreeze() {
        val result = StreakCalculator.calculate((1L..7L).map { day(it, 90.0) })
        assertEquals(1, result.freezes)
        assertEquals(0, result.progress)
    }

    @Test
    fun fourteenQualifyingDaysEarnTwoFreezes() {
        val result = StreakCalculator.calculate((1L..14L).map { day(it, 90.0) })
        assertEquals(2, result.freezes)
        assertEquals(0, result.progress)
    }

    @Test
    fun fifteenQualifyingDaysCarryOneDay() {
        val result = StreakCalculator.calculate((1L..15L).map { day(it, 90.0) })
        assertEquals(2, result.freezes)
        assertEquals(1, result.progress)
    }

    @Test
    fun scoreBetweenEightyAndEightyFiveKeepsStreakWithoutProgress() {
        val result = StreakCalculator.calculate(listOf(day(1, 82.0), day(2, 84.9)))
        assertEquals(2, result.current)
        assertEquals(0, result.progress)
    }

    @Test
    fun existingNumericProgressIsPreservedUnderNewRequirement() {
        val result = StreakCalculator.calculate((1L..4L).map { day(it, 90.0) })
        assertEquals(0, result.freezes)
        assertEquals(4, result.progress)
    }

    @Test
    fun freezeCapStillApplies() {
        val result = StreakCalculator.calculate((1L..35L).map { day(it, 90.0) })
        assertEquals(StreakRules.MAX_STORED_FREEZES, result.freezes)
        assertEquals(14, result.progress)
    }

    @Test
    fun failedDayBreaksWithoutFreeze() {
        val result = StreakCalculator.calculate(listOf(day(1, 90.0), day(2, 20.0, false)))
        assertEquals(0, result.current)
        assertEquals(1, result.best)
    }
}
