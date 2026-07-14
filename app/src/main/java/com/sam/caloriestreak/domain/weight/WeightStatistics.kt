package com.sam.caloriestreak.domain.weight

import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

data class WeightStats(
    val latest: Double? = null,
    val previous: Double? = null,
    val first: Double? = null,
    val changeFromPrevious: Double? = null,
    val changeFromFirst: Double? = null,
    val lowest: Double? = null,
    val highest: Double? = null,
    val averageWeek: Double? = null,
    val averageMonth: Double? = null,
    val averageYear: Double? = null,
    val averageAll: Double? = null,
    val entryCount: Int = 0,
    val distinctDates: Int = 0,
    val longestGapDays: Long = 0,
    val mostRecentTimestamp: Long? = null
)

object WeightStatistics {
    fun calculate(entries: List<WeightEntryEntity>, now: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()): WeightStats {
        if (entries.isEmpty()) return WeightStats()
        val sorted = entries.sortedBy { it.timestamp }
        val latest = sorted.last()
        val previous = sorted.getOrNull(sorted.lastIndex - 1)
        val first = sorted.first()
        fun date(entry: WeightEntryEntity) = Instant.ofEpochMilli(entry.timestamp).atZone(zone).toLocalDate()
        fun average(days: Long): Double? {
            val start = now.minusDays(days - 1)
            val values = sorted.filter { !date(it).isBefore(start) && !date(it).isAfter(now) }
            return values.map { it.kilograms }.average().takeUnless { it.isNaN() }
        }
        val gaps = sorted.zipWithNext().map { (a, b) -> abs(date(b).toEpochDay() - date(a).toEpochDay()) }
        return WeightStats(
            latest = latest.kilograms,
            previous = previous?.kilograms,
            first = first.kilograms,
            changeFromPrevious = previous?.let { latest.kilograms - it.kilograms },
            changeFromFirst = latest.kilograms - first.kilograms,
            lowest = sorted.minOf { it.kilograms },
            highest = sorted.maxOf { it.kilograms },
            averageWeek = average(7),
            averageMonth = average(30),
            averageYear = average(365),
            averageAll = sorted.map { it.kilograms }.average(),
            entryCount = sorted.size,
            distinctDates = sorted.map(::date).distinct().size,
            longestGapDays = gaps.maxOrNull() ?: 0,
            mostRecentTimestamp = latest.timestamp
        )
    }
}
