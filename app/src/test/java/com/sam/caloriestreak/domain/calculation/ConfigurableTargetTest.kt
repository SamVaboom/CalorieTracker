package com.sam.caloriestreak.domain.calculation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigurableTargetTest {
    @Test fun defaultTargetIs1650() {
        assertEquals(1650.0, ScoreCalculator.DEFAULT_TARGET, 0.0)
    }

    @Test fun configuredTargetIsExactHundredPoint() {
        val calculator = ScoreCalculator.forTarget(2000.0)
        assertEquals(100.0, calculator.calculate(2000.0), 0.000001)
    }

    @Test fun anchorsScaleProportionally() {
        val points = ScoreCalculator.pointsForTarget(3300.0)
        assertEquals(1600.0, points[0].calories, 0.000001)
        assertEquals(2400.0, points[1].calories, 0.000001)
        assertEquals(2800.0, points[2].calories, 0.000001)
        assertEquals(3300.0, points[3].calories, 0.000001)
        assertEquals(4400.0, points.last().calories, 0.000001)
    }

    @Test fun invalidTargetsAreRejected() {
        assertTrue(runCatching { ScoreCalculator.pointsForTarget(799.0) }.isFailure)
        assertTrue(runCatching { ScoreCalculator.pointsForTarget(5001.0) }.isFailure)
    }
}
