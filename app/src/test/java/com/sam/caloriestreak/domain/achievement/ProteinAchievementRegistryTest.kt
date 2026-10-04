package com.sam.caloriestreak.domain.achievement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProteinAchievementRegistryTest {
    @Test fun registryContainsEveryRequestedProteinAchievementExactlyOnce() {
        val expected = setOf(
            "protein_initiate",
            "protein_solid_foundation",
            "protein_triple_digits",
            "protein_heavy_lifter",
            "protein_150_club",
            "protein_absolute_unit",
            "protein_snack",
            "protein_power_meal",
            "protein_main_course",
            "protein_bomb",
            "protein_kilo_club",
            "protein_five_kilo_sack",
            "protein_ten_kilo_plate",
            "protein_warehouse",
            "protein_industrial_quantities",
            "protein_empire",
            "protein_pound_for_pound",
            "protein_double_density",
            "protein_half_of_yourself",
            "protein_eat_yourself",
            "protein_three_days",
            "protein_week",
            "protein_routine",
            "protein_triple_digit_streak",
            "protein_detective",
            "protein_librarian",
            "protein_no_mystery_macros",
            "protein_fully_calculated",
            "protein_complete_week",
            "protein_chef",
            "protein_high_protein_menu",
            "protein_explorer"
        )
        val definitions = AchievementRegistry.protein
        assertEquals(expected, definitions.map { it.id }.toSet())
        assertEquals(expected.size, definitions.size)
        assertTrue(definitions.all { it.category == AchievementCategory.PROTEIN })
        assertTrue(definitions.single { it.id == "protein_absolute_unit" }.hidden)
        assertEquals(setOf("protein_no_mystery_macros"), AchievementRegistry.revocableProteinIds)
        assertFalse(definitions.any { it.repeatable })
    }
}
