package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreezeTodayPolicyTest {
    @Test
    fun buttonRequiresAvailableFreezeAndUnfrozenDay() {
        assertFalse(FreezeTodayPolicy.canUseFreeze(0, alreadyFrozen = false))
        assertFalse(FreezeTodayPolicy.canUseFreeze(2, alreadyFrozen = true))
        assertTrue(FreezeTodayPolicy.canUseFreeze(1, alreadyFrozen = false))
    }

    @Test
    fun frozenDayUsesEffectiveHundredButKeepsActualScore() {
        val actual = 20.0
        assertEquals(100.0, FreezeTodayPolicy.effectiveScore(actual, frozen = true), 0.0001)
        assertEquals(actual, FreezeTodayPolicy.effectiveScore(actual, frozen = false), 0.0001)
    }

    @Test
    fun frozenDayQualifiesOnlyFromActualScore() {
        assertFalse(FreezeTodayPolicy.qualifiesForProgress(84.99))
        assertTrue(FreezeTodayPolicy.qualifiesForProgress(85.0))
    }

    @Test
    fun inProgressFreezeConsumesInventoryWithoutAddingStreakDay() {
        val earnedDays = (1L..7L).map { day -> qualifyingDay(day) }
        val before = StreakCalculator.calculate(earnedDays)
        val frozenToday = DailyLogEntity(
            dateEpochDay = 8,
            totalCalories = 2300.0,
            score = 0.0,
            finalized = false,
            streakSuccessful = false,
            freezeUsed = true,
            manualCheatDay = true,
            freezeQualifying = false,
            createdAt = 8,
            updatedAt = 8
        )
        val after = StreakCalculator.calculate(earnedDays + frozenToday)

        assertEquals(1, before.freezes)
        assertEquals(0, after.freezes)
        assertEquals(before.current, after.current)
    }

    private fun qualifyingDay(day: Long) = DailyLogEntity(
        dateEpochDay = day,
        totalCalories = 1650.0,
        score = 100.0,
        finalized = true,
        streakSuccessful = true,
        freezeUsed = false,
        manualCheatDay = false,
        freezeQualifying = true,
        createdAt = day,
        updatedAt = day
    )
}
