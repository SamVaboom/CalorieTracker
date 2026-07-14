package com.sam.caloriestreak.domain.achievement

import com.sam.caloriestreak.data.local.entity.ActivityEventEntity
import com.sam.caloriestreak.data.local.entity.ActivityEventType
import com.sam.caloriestreak.data.local.entity.DailyLogEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtherAchievementRulesTest {
    private fun meal(id: String, day: Long, recipe: String?, timeMillis: Long = day) = MealLogEntity(
        id = id,
        dateEpochDay = day,
        timeMillis = timeMillis,
        recipeId = recipe,
        recipeName = recipe ?: "Manual",
        portionDescription = "1",
        portionMultiplier = 1.0,
        calories = 100.0,
        createdAt = 1,
        updatedAt = 1
    )

    private fun daily(
        day: Long,
        calories: Double,
        score: Double,
        freezeUsed: Boolean = false,
        qualifying: Boolean = false,
        manualFreeze: Boolean = false,
        target: Double = 1650.0
    ) = DailyLogEntity(
        dateEpochDay = day,
        totalCalories = calories,
        score = score,
        finalized = true,
        streakSuccessful = score >= 80,
        freezeUsed = freezeUsed,
        manualCheatDay = manualFreeze,
        freezeQualifying = qualifying,
        createdAt = 1,
        updatedAt = 1,
        targetCalories = target
    )

    @Test fun pickyEaterUsesRollingThirtyDayWindowAndIgnoresManualEntries() {
        val logs = (0 until 15).map { meal("r$it", it.toLong(), "recipe-a") } +
            (0 until 25).map { meal("m$it", it.toLong(), null) }
        val ids = AchievementEvaluator.eligibleMealAndRecipeAchievements(logs, 1, 1)
        assertTrue("picky_eater" in ids)
        assertTrue("freestyler" in ids)
        assertFalse("explorer" in ids)
    }

    @Test fun mealPrepUsesConsecutiveDates() {
        val logs = (1L..3L).map { day -> meal("$day", day, "same") }
        val ids = AchievementEvaluator.eligibleMealAndRecipeAchievements(logs, 1, 1)
        assertTrue("meal_prep" in ids)
    }

    @Test fun calorieAndScoreRulesUseFinalizedSnapshots() {
        val logs = buildList {
            add(daily(1, 900.0, 40.0))
            add(daily(2, 700.0, 0.0))
            add(daily(3, 3100.0, 0.0))
            add(daily(4, 2600.0, 82.0, manualFreeze = true))
            (5L..11L).forEach { add(daily(it, 1650.0, 100.0)) }
        }
        val ids = AchievementEvaluator.eligibleCalorieAndScoreAchievements(logs)
        assertTrue("fasting_monk" in ids)
        assertTrue("air_diet" in ids)
        assertTrue("the_american" in ids)
        assertTrue("cheat_code" in ids)
        assertTrue("bullseye" in ids)
        assertTrue("three_in_a_row" in ids)
        assertTrue("perfectionist" in ids)
    }

    @Test fun freezeRulesUseEarnedAndConsumedHistory() {
        val qualifying = (1L..7L).map { daily(it, 1500.0, 90.0, qualifying = true) }
        val protected = daily(8, 2500.0, 0.0, freezeUsed = true)
        val events = listOf(ActivityEventEntity("last", ActivityEventType.LAST_FREEZE_USED, 8, 8))
        val ids = AchievementEvaluator.eligibleFreezeAchievements(qualifying + protected, currentFreezes = 0, events = events)
        assertTrue("winter_is_coming" in ids)
        assertTrue("close_call" in ids)
        assertTrue("yolo" in ids)
    }

    @Test fun weightAchievementsFollowLatestWeightAndGoal() {
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
        val ids = AchievementEvaluator.eligibleWeightAchievements(entries, weightGoal = 90.0, zone = zone)
        assertTrue("weight_first_entry" in ids)
        assertTrue("weight_7_dates" in ids)
        assertTrue("weight_loss_05" in ids)
        assertTrue("weight_halfway" in ids)
        assertTrue("weight_phoenix" in ids)
    }

    @Test fun regainedWeightRevokesLossMilestones() {
        val zone = ZoneOffset.UTC
        val first = LocalDate.of(2026, 1, 1)
        val entries = listOf(
            WeightEntryEntity("a", 100.0, first.atStartOfDay(zone).toInstant().toEpochMilli(), createdAt = 1, updatedAt = 1),
            WeightEntryEntity("b", 88.0, first.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(), createdAt = 1, updatedAt = 1),
            WeightEntryEntity("c", 97.0, first.plusDays(2).atStartOfDay(zone).toInstant().toEpochMilli(), createdAt = 1, updatedAt = 1)
        )
        val ids = AchievementEvaluator.eligibleWeightAchievements(entries, weightGoal = 90.0, zone = zone)
        assertTrue("weight_loss_03" in ids)
        assertFalse("weight_loss_05" in ids)
        assertFalse("weight_goal" in ids)
        assertFalse("weight_phoenix" in ids)
    }

    @Test fun groceryAndAppOpenEventsUnlockActivityAchievements() {
        val events = buildList {
            repeat(10) { add(ActivityEventEntity("g$it", ActivityEventType.GROCERY_GENERATED, it.toLong(), it.toLong())) }
            add(ActivityEventEntity("done", ActivityEventType.GROCERY_COMPLETED, 10, 10))
            repeat(30) { add(ActivityEventEntity("open$it", ActivityEventType.APP_OPEN, it.toLong(), it.toLong())) }
        }
        val ids = AchievementEvaluator.eligibleActivityAchievements(events)
        assertTrue("shopping_spree" in ids)
        assertTrue("organized" in ids)
        assertTrue("touch_grass" in ids)
    }

    @Test fun hiddenAchievementsRemainHiddenUntilEarned() {
        assertTrue(AchievementRegistry.all.first { it.id == "air_diet" }.hidden)
        assertTrue(AchievementRegistry.all.first { it.id == "ghost" }.hidden)
        assertTrue(AchievementRegistry.all.first { it.id == "bottomless_pit" }.hidden)
    }
}
