package com.sam.caloriestreak.domain.protein

import java.text.NumberFormat
import java.util.Locale

object ProteinFormatter {
    fun grams(value: Double, locale: Locale = Locale.getDefault()): String =
        "${decimal(value, locale)} g"

    fun known(summary: ProteinSummary, locale: Locale = Locale.getDefault()): String = when {
        !summary.hasKnownData -> "Protein unknown"
        summary.complete -> "${grams(summary.knownGrams, locale)} protein"
        else -> "${grams(summary.knownGrams, locale)} known protein"
    }

    fun lifetime(value: Double, locale: Locale = Locale.getDefault()): String =
        if (value >= 1_000.0) "${decimal(value / 1_000.0, locale)} kg" else grams(value, locale)

    private fun decimal(value: Double, locale: Locale): String = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
        isGroupingUsed = true
    }.format(value)
}
