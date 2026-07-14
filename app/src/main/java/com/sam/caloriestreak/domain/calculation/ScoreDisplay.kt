package com.sam.caloriestreak.domain.calculation

import kotlin.math.roundToInt

object ScoreDisplay {
    fun percent(score: Double): Int = score.coerceIn(0.0, 100.0).roundToInt()
}
