package com.sam.caloriestreak.domain.achievement

import com.sam.caloriestreak.data.local.entity.MealLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEvaluatorTest {
    private fun meal(day: Long, id: String = day.toString()) = MealLogEntity(
        id = id,
        dateEpochDay = day,
        timeMillis = day,
        recipeId = null,
        recipeName = "Manual",
        portionDescription = "Manual",
        portionMultiplier = 1.0,
        calories = 100.0,
        createdAt = 1,
        updatedAt = 1
    )

    @Test fun everyConfiguredTimeThresholdUnlocksExactlyAtThreshold() {
        AchievementRegistry.time.forEach { definition ->
            val exact = (1L..definition.requiredDays.toLong()).map(::meal)
            val early = exact.dropLast(1)
            assertTrue(definition.id in AchievementEvaluator.eligibleTimeAchievements(exact).map { it.id })
            assertFalse(definition.id in AchievementEvaluator.eligibleTimeAchievements(early).map { it.id })
        }
    }

    @Test fun missingRecordedDayBreaksChain() {
        val meals = listOf(meal(1), meal(2), meal(4), meal(5), meal(6))
        assertEquals(3, AchievementEvaluator.consecutiveRecordedDays(meals))
    }

    @Test fun deletingOnlyMealChangesChain() {
        val complete = (1L..5L).map(::meal)
        val deletedDayThree = complete.filterNot { it.dateEpochDay == 3L }
        assertEquals(5, AchievementEvaluator.consecutiveRecordedDays(complete))
        assertEquals(2, AchievementEvaluator.consecutiveRecordedDays(deletedDayThree))
    }

    @Test fun registryContainsOnlySpecifiedTimeThresholds() {
        assertEquals(
            listOf(1, 3, 5, 8, 12, 14, 16, 21, 26, 30, 36, 45, 46, 52, 56, 65, 69, 78, 80, 82, 88, 96, 140, 259, 274, 300, 365, 420),
            AchievementRegistry.time.map { it.requiredDays }
        )
    }
}
