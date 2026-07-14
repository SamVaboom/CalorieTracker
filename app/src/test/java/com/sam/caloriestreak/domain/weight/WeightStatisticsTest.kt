package com.sam.caloriestreak.domain.weight

import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeightStatisticsTest {
    private val zone = ZoneOffset.UTC
    private fun entry(id: String, kg: Double, date: LocalDate) = WeightEntryEntity(
        id = id,
        kilograms = kg,
        timestamp = date.atStartOfDay(zone).toInstant().toEpochMilli(),
        createdAt = 1,
        updatedAt = 1
    )

    @Test fun emptyStatisticsRemainEmpty() {
        val result = WeightStatistics.calculate(emptyList(), LocalDate.of(2026, 7, 14), zone)
        assertNull(result.latest)
        assertEquals(0, result.entryCount)
    }

    @Test fun calculatesLatestPreviousFirstAndExtremes() {
        val entries = listOf(
            entry("a", 96.2, LocalDate.of(2026, 1, 1)),
            entry("b", 95.0, LocalDate.of(2026, 2, 1)),
            entry("c", 94.2, LocalDate.of(2026, 3, 1))
        )
        val result = WeightStatistics.calculate(entries, LocalDate.of(2026, 3, 1), zone)
        assertEquals(94.2, result.latest!!, 0.0001)
        assertEquals(95.0, result.previous!!, 0.0001)
        assertEquals(96.2, result.first!!, 0.0001)
        assertEquals(-0.8, result.changeFromPrevious!!, 0.0001)
        assertEquals(-2.0, result.changeFromFirst!!, 0.0001)
        assertEquals(94.2, result.lowest!!, 0.0001)
        assertEquals(96.2, result.highest!!, 0.0001)
    }

    @Test fun averagesOnlyActualEntriesInsideRanges() {
        val now = LocalDate.of(2026, 7, 14)
        val entries = listOf(
            entry("week", 90.0, now.minusDays(2)),
            entry("month", 100.0, now.minusDays(20)),
            entry("year", 110.0, now.minusDays(200)),
            entry("old", 120.0, now.minusDays(500))
        )
        val result = WeightStatistics.calculate(entries, now, zone)
        assertEquals(90.0, result.averageWeek!!, 0.0001)
        assertEquals(95.0, result.averageMonth!!, 0.0001)
        assertEquals(100.0, result.averageYear!!, 0.0001)
        assertEquals(105.0, result.averageAll!!, 0.0001)
    }
}
