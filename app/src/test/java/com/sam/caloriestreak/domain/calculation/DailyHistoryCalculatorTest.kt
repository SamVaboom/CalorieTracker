package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyHistoryCalculatorTest {
    @Test
    fun deletingOneDuplicateOnlyRemovesItsCalories() {
        val first = meal("first", day = 1, calories = 700.0)
        val duplicate = meal("duplicate", day = 1, calories = 700.0)

        val before = DailyHistoryCalculator.rebuildCompletedDays(
            meals = listOf(first, duplicate),
            existing = emptyList(),
            todayEpochDay = 2,
            now = 1
        )
        val after = DailyHistoryCalculator.rebuildCompletedDays(
            meals = listOf(first),
            existing = before,
            todayEpochDay = 2,
            now = 2
        )

        assertEquals(1400.0, before.single().totalCalories, 0.0001)
        assertEquals(700.0, after.single().totalCalories, 0.0001)
        assertEquals(0.0, after.single().score, 0.0001)
    }

    @Test
    fun deletingHistoricalMealRebuildsLaterStreak() {
        val meals = listOf(
            meal("day-one", day = 1, calories = 1650.0),
            meal("day-two", day = 2, calories = 1650.0)
        )
        val before = DailyHistoryCalculator.rebuildCompletedDays(
            meals = meals,
            existing = emptyList(),
            todayEpochDay = 3,
            now = 1
        )
        val after = DailyHistoryCalculator.rebuildCompletedDays(
            meals = meals.filterNot { it.id == "day-one" },
            existing = before,
            todayEpochDay = 3,
            now = 2
        )

        assertEquals(2, StreakCalculator.calculate(before).current)
        assertEquals(1, StreakCalculator.calculate(after).current)
        assertEquals(0.0, after.first().totalCalories, 0.0001)
        assertTrue(after.last().streakSuccessful)
    }

    @Test
    fun staleExistingDayIsRecalculatedToZeroWhenLastMealIsDeleted() {
        val existing = listOf(
            DailyLogEntity(
                dateEpochDay = 1,
                totalCalories = 1650.0,
                score = 100.0,
                finalized = true,
                streakSuccessful = true,
                createdAt = 1,
                updatedAt = 1
            )
        )

        val rebuilt = DailyHistoryCalculator.rebuildCompletedDays(
            meals = emptyList(),
            existing = existing,
            todayEpochDay = 2,
            now = 2
        )

        assertEquals(0.0, rebuilt.single().totalCalories, 0.0001)
        assertEquals(0.0, rebuilt.single().score, 0.0001)
        assertTrue(!rebuilt.single().streakSuccessful)
    }

    private fun meal(id: String, day: Long, calories: Double) = MealLogEntity(
        id = id,
        dateEpochDay = day,
        timeMillis = day,
        recipeName = "Meal",
        portionDescription = "1 serving",
        portionMultiplier = 1.0,
        calories = calories,
        createdAt = day,
        updatedAt = day
    )
}