package com.sam.caloriestreak.ui.dashboard

object ScoreRingGeometry {
    const val START_ANGLE_DEGREES = -90f

    fun progress(scorePercent: Double): Float =
        (scorePercent.coerceIn(0.0, 100.0) / 100.0).toFloat()

    fun sweepDegrees(progress: Float): Float =
        360f * progress.coerceIn(0f, 1f)

    fun endAngleDegrees(scorePercent: Double): Float =
        normalizeDegrees(START_ANGLE_DEGREES + sweepDegrees(progress(scorePercent)))

    private fun normalizeDegrees(angle: Float): Float = ((angle % 360f) + 360f) % 360f
}
