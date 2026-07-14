package com.sam.caloriestreak.domain.achievement

import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtherAchievementRulesTest {
    private fun meal(id: String, day: Long, recipe: String?) = MealLogEntity(
        id = id,
        dateEpochDay = day,
        timeMillis = day,
        recipeId = recipe,
        recipeName = recipe ?: "Manual",
        portionDescription = "1",
        portionMultiplier = 1.0,
        calories = 100.0,
        createdAt = 1,
        updatedAt = 1
    )

    private fun daily(day: Long, calories: Double, score: Double, freezeUsed: Boolean = false, qualifying: Boolean = false) = DailyLogEntity(
        dateEpochDay = day,
        totalCalories = calories,
        score = score,
        finalized = true,
        streakSuccessful = score >= 80,
        freezeUsed = freezeUsed,
        manualCheatDay = false,
        freezeQualifying = qualifying,
        createdAt = 1,
        updatedAt = 1
    )

    @Test fun pickyEaterUsesRollingThirtyDayWindowAndIgnoresManualEntries() {
        val logs = (0 until 15).map { meal("r$it", it.toLong(), "recipe-a") } +
            (0 until 20).map { meal("m$it", it.toLong(), null) }
        val ids = AchievementEvaluator.eligibleMealAndRecipeAchievements(logs, 1, 1)
        assertTrue("picky_eater" in ids)
        assertFalse("explorer" in ids)
    }

    @Test fun creatureOfHabitAndMealPrepperUseDates() {
        val logs = (1L..7L).flatMap { day -> listOf(meal("$day-a", day, "same"), meal("$day-b", day, "same")) }
        val ids = AchievementEvaluator.eligibleMealAndRecipeAchievements(logs, 1, 1)
        assertTrue("creature_of_habit" in ids)
        assertTrue("meal_prepper" in ids)
    }

    @Test fun calorieAndScoreRulesUseFinalizedSnapshots() {
        val logs = listOf(
            daily(1, 900.0, 40.0),
            daily(2, 700.0, 0.0),
            daily(3, 3100.0, 0.0),
            daily(4, 1650.0, 100.0),
            daily(5, 1650.0, 100.0),
            daily(6, 1650.0, 100.0)
        )
        val ids = AchievementEvaluator.eligibleCalorieAndScoreAchievements(logs)
        assertTrue("fasting_monk" in ids)
        assertTrue("air_diet" in ids)
        assertTrue("the_american" in ids)
        assertTrue("bullseye" in ids)
        assertTrue("double_bullseye" in ids)
        assertTrue("hat_trick" in ids)
    }

    @Test fun freezeRulesUseEarnedAndConsumedHistory() {
        val qualifying = (1L..7L).map { daily(it, 1500.0, 90.0, qualifying = true) }
        val protected = daily(8, 2500.0, 0.0, freezeUsed = true)
        val ids = AchievementEvaluator.eligibleFreezeAchievements(qualifying + protected, currentFreezes = 0)
        assertTrue("winter_is_coming" in ids)
        assertTrue("seven_good_days" in ids)
        assertTrue("close_call" in ids)
    }

    @Test fun weightAchievementsUseActualEntriesAndDistinctDates() {
        val zone = ZoneOffset.UTC
        val first = LocalDate.of(2026, 1, 1)
        val entries = (0 until 7).map { index ->
            WeightEntryEntity(
                id = index.toString(),
                kilograms = 100.0 - index,
                timestamp = first.plusDays(index.toLong()).atStartOfDay(zone).toInstant().toEpochMilli(),
                createdAt = 1,
                updatedAt = 1
            )
        }
        val ids = AchievementEvaluator.eligibleWeightAchievements(entries, zone).map { it.id }
        assertTrue("weight_first" in ids)
        assertTrue("weight_7_dates" in ids)
        assertTrue("weight_down_5" in ids)
        assertTrue("weight_new_low" in ids)
    }

    @Test fun hiddenAchievementsRemainHiddenUntilEarned() {
        assertTrue(AchievementRegistry.all.first { it.id == "air_diet" }.hidden)
        assertTrue(AchievementRegistry.all.first { it.id == "bottomless_pit" }.hidden)
    }
}
