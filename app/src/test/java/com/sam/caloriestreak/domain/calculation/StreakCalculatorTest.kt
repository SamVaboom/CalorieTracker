package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {
    private fun day(index: Long, score: Double, success: Boolean = score >= 80, freeze: Boolean = false) =
        DailyLogEntity(index, 1650.0, score, true, success, freeze, false, score >= 85, 0, 0)

    @Test fun streakAndFreezeProgress() {
        val result = StreakCalculator.calculate(listOf(day(1, 90.0), day(2, 82.0), day(3, 95.0)))
        assertEquals(3, result.current)
        assertEquals(2, result.progress)
    }

    @Test fun fiveQualifyingDaysEarnFreeze() {
        val result = StreakCalculator.calculate((1L..5L).map { day(it, 90.0) })
        assertEquals(1, result.freezes)
        assertEquals(0, result.progress)
    }

    @Test fun failedDayBreaksWithoutFreeze() {
        val result = StreakCalculator.calculate(listOf(day(1, 90.0), day(2, 20.0, false)))
        assertEquals(0, result.current)
        assertEquals(1, result.best)
    }
}
