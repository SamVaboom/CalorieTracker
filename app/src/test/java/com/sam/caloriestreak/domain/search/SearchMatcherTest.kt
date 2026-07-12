package com.sam.caloriestreak.domain.search

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchMatcherTest {
    @Test
    fun matchingIsCaseInsensitiveAndPartial() {
        assertTrue(SearchMatcher.matches("PIZ", "Eggplant Pizza"))
        assertTrue(SearchMatcher.matches("moz", "Mozzarella"))
    }

    @Test
    fun leadingAndTrailingWhitespaceIsIgnored() {
        assertTrue(SearchMatcher.matches("  banana  ", "Banana"))
    }

    @Test
    fun blankQueryShowsEverything() {
        assertTrue(SearchMatcher.matches("   ", "Anything"))
    }

    @Test
    fun searchesAcrossOptionalFields() {
        assertTrue(SearchMatcher.matches("alnatura", "Tomato sauce", "Alnatura", null))
        assertFalse(SearchMatcher.matches("coop", "Tomato sauce", "Alnatura", null))
    }
}