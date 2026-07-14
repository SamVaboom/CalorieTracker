package com.sam.caloriestreak.domain.calculation

data class FreezeRuleBaseline(
    val cutoffEpochDay: Long,
    val freezes: Int,
    val progress: Int
)
