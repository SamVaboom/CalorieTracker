package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.MealLogEntity
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class CalorieStatisticsCalculatorTest {
    private val today = LocalDate.of(2026, 10, 4).toEpochDay()
    private fun meal(day: Long, calories: Double) = MealLogEntity(
        id = "$day-$calories", dateEpochDay = day, timeMillis = 0, recipeName = "Meal",
        portionDescription = "Manual", portionMultiplier = 1.0, calories = calories, createdAt = 0, updatedAt = 0
    )
    @Test fun completedCalendarWindowsExcludeTodayAndOutsideBoundaries() {
        val meals = (0L..31L).map { meal(today - it, 1000.0 + it) } + meal(today + 1, 9999.0)
        val week = CalorieStatisticsCalculator.completedDays(meals, today, 7)
        val month = CalorieStatisticsCalculator.completedDays(meals, today, 30)
        assertEquals(7, week.recordedDays)
        assertEquals(1004.0, week.averageCalories!!, 0.001)
        assertEquals(30, month.recordedDays)
        assertEquals(1015.5, month.averageCalories!!, 0.001)
    }
    @Test fun missingDaysStayUnknownButExplicitZeroIsIncluded() {
        val meals = listOf(meal(today - 1, 1000.0), meal(today - 1, 500.0), meal(today - 7, 0.0), meal(today, 9999.0))
        val result = CalorieStatisticsCalculator.completedDays(meals, today, 7)
        assertEquals(2, result.recordedDays)
        assertEquals(750.0, result.averageCalories!!, 0.001)
        assertEquals(7, result.calendarDays)
    }
    @Test fun noCompletedRecordsHaveNoAverage() {
        val result = CalorieStatisticsCalculator.completedDays(listOf(meal(today, 1000.0)), today, 30)
        assertNull(result.averageCalories)
        assertNull(result.averageScore(ScoreCalculator.forTarget(1650.0)))
        assertEquals(0, result.recordedDays)
    }
    @Test fun calendarWindowCrossesYearAndLeapDay() {
        listOf(LocalDate.of(2026, 1, 1), LocalDate.of(2024, 3, 1)).forEach { date ->
            val end = date.toEpochDay()
            val result = CalorieStatisticsCalculator.completedDays(listOf(meal(end - 7, 800.0), meal(end - 8, 9000.0)), end, 7)
            assertEquals(800.0, result.averageCalories!!, 0.001)
        }
    }
}
