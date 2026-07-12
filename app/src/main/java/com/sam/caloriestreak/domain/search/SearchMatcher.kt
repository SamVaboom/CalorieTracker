package com.sam.caloriestreak.domain.search

object SearchMatcher {
    fun normalized(query: String): String = query.trim().lowercase()

    fun matches(query: String, vararg values: String?): Boolean {
        val normalizedQuery = normalized(query)
        if (normalizedQuery.isEmpty()) return true
        return values.any { value -> value?.lowercase()?.contains(normalizedQuery) == true }
    }
}