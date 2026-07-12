package com.sam.caloriestreak.domain.history

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySeriesTest {
    @Test
    fun defaultsAreScoreAndLastWeek() {
        assertEquals(HistoryMetric.SCORE, HistoryGraphDefaults.metric)
        assertEquals(HistoryRange.WEEK, HistoryGraphDefaults.range)
    }

    @Test
    fun rollingRangesContainExpectedNumberOfDays() {
        assertEquals(7, build(HistoryRange.WEEK).size)
        assertEquals(30, build(HistoryRange.MONTH).size)
        assertEquals(365, build(HistoryRange.YEAR).size)
    }

    @Test
    fun allTimeStartsAtFirstStoredDateAndSortsChronologically() {
        val points = HistorySeriesBuilder.build(
            meals = listOf(meal("later", 10, 100.0), meal("first", 8, 200.0)),
            dailyLogs = emptyList(),
            range = HistoryRange.ALL,
            todayEpochDay = 10
        )

        assertEquals(listOf(8L, 9L, 10L), points.map { it.epochDay })
        assertEquals(200.0, points.first().calories, 0.0001)
        assertEquals(100.0, points.last().calories, 0.0001)
    }

    @Test
    fun daysWithoutMealsUseZeroCaloriesConsistently() {
        val points = HistorySeriesBuilder.build(
            meals = listOf(meal("today", 10, 1650.0)),
            dailyLogs = emptyList(),
            range = HistoryRange.WEEK,
            todayEpochDay = 10
        )

        assertTrue(points.dropLast(1).all { it.calories == 0.0 })
        assertEquals(100.0, points.last().score, 0.0001)
    }

    @Test
    fun frozenDayKeepsActualCaloriesAndActualScore() {
        val frozen = DailyLogEntity(
            dateEpochDay = 10,
            totalCalories = 2300.0,
            score = 0.0,
            finalized = true,
            streakSuccessful = false,
            freezeUsed = true,
            manualCheatDay = true,
            freezeQualifying = false,
            createdAt = 1,
            updatedAt = 1
        )
        val points = HistorySeriesBuilder.build(
            meals = listOf(meal("restaurant", 10, 2300.0)),
            dailyLogs = listOf(frozen),
            range = HistoryRange.ALL,
            todayEpochDay = 10
        )

        assertEquals(2300.0, points.single().value(HistoryMetric.CALORIES), 0.0001)
        assertEquals(0.0, points.single().value(HistoryMetric.SCORE), 0.0001)
        assertTrue(points.single().freezeProtected)
    }

    @Test
    fun emptyAllTimeSeriesHasEmptyState() {
        val points = HistorySeriesBuilder.build(
            meals = emptyList(),
            dailyLogs = emptyList(),
            range = HistoryRange.ALL,
            todayEpochDay = 10
        )
        assertTrue(points.isEmpty())
        assertFalse(HistorySeriesBuilder.hasTrackedData(points, emptyList()))
    }

    private fun build(range: HistoryRange) = HistorySeriesBuilder.build(
        meals = listOf(meal("today", 100, 1650.0)),
        dailyLogs = emptyList(),
        range = range,
        todayEpochDay = 100
    )

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