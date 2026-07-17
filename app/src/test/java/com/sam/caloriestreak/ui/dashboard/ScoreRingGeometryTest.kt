package com.sam.caloriestreak.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreRingGeometryTest {
    @Test
    fun zeroPercentStartsAtTwelveOClock() {
        assertEquals(-90f, ScoreRingGeometry.START_ANGLE_DEGREES, 0.0001f)
        assertEquals(270f, ScoreRingGeometry.endAngleDegrees(0.0), 0.0001f)
        assertEquals(0f, ScoreRingGeometry.sweepDegrees(ScoreRingGeometry.progress(0.0)), 0.0001f)
    }

    @Test
    fun quarterProgressEndsAtThreeOClock() {
        assertEquals(90f, ScoreRingGeometry.sweepDegrees(ScoreRingGeometry.progress(25.0)), 0.0001f)
        assertEquals(0f, ScoreRingGeometry.endAngleDegrees(25.0), 0.0001f)
    }

    @Test
    fun halfProgressEndsAtSixOClock() {
        assertEquals(180f, ScoreRingGeometry.sweepDegrees(ScoreRingGeometry.progress(50.0)), 0.0001f)
        assertEquals(90f, ScoreRingGeometry.endAngleDegrees(50.0), 0.0001f)
    }

    @Test
    fun threeQuarterProgressEndsAtNineOClock() {
        assertEquals(270f, ScoreRingGeometry.sweepDegrees(ScoreRingGeometry.progress(75.0)), 0.0001f)
        assertEquals(180f, ScoreRingGeometry.endAngleDegrees(75.0), 0.0001f)
    }

    @Test
    fun fullProgressCompletesClockwiseCircle() {
        val sweeps = listOf(0.0, 25.0, 50.0, 75.0, 100.0).map {
            ScoreRingGeometry.sweepDegrees(ScoreRingGeometry.progress(it))
        }
        assertEquals(360f, sweeps.last(), 0.0001f)
        assertTrue(sweeps.zipWithNext().all { (first, second) -> second > first })
        assertEquals(270f, ScoreRingGeometry.endAngleDegrees(100.0), 0.0001f)
    }
}
