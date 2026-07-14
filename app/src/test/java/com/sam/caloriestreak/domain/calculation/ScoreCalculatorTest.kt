package com.sam.caloriestreak.domain.calculation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreCalculatorTest {
    private val calculator = ScoreCalculator()

    @Test fun exactPoints() {
        ScoreCalculator.defaultPoints.forEach { point ->
            assertEquals(point.score, calculator.calculate(point.calories), 0.0001)
        }
    }

    @Test fun exactTargetReturnsAndDisplaysHundred() {
        assertEquals(100.0, calculator.calculate(1650.0), 0.0)
        assertEquals(100, ScoreDisplay.percent(calculator.calculate(1650.0)))
    }

    @Test fun tinyFloatingNoiseAroundTargetStillReturnsHundred() {
        assertEquals(100.0, calculator.calculate(1650.0 - 0.0000001), 0.0)
        assertEquals(100.0, calculator.calculate(1650.0 + 0.0000001), 0.0)
    }

    @Test fun displayUsesMathematicalRoundingNotFlooring() {
        assertEquals(100, ScoreDisplay.percent(99.6))
        assertEquals(99, ScoreDisplay.percent(99.2))
        assertEquals(85, ScoreDisplay.percent(84.5))
        assertEquals(84, ScoreDisplay.percent(84.4))
    }

    @Test fun interpolatesAndDescendsAboveTarget() {
        assertEquals(60.0, calculator.calculate(1300.0), 0.0001)
        assertEquals(90.0, calculator.calculate(1525.0), 0.0001)
        assertTrue(calculator.calculate(1800.0) < calculator.calculate(1650.0))
    }

    @Test fun scoresAlwaysRemainWithinBounds() {
        listOf(-1000.0, 0.0, 800.0, 1650.0, 1800.0, 2200.0, 10000.0).forEach {
            assertTrue(calculator.calculate(it) in 0.0..100.0)
        }
    }

    @Test fun scalableTargetUsesProportionalAnchors() {
        val points = ScoreCalculator.pointsForTarget(3300.0)
        assertEquals(1600.0, points[0].calories, 0.0001)
        assertEquals(3300.0, points[3].calories, 0.0001)
        assertEquals(4400.0, points.last().calories, 0.0001)
        assertEquals(100.0, ScoreCalculator.forTarget(3300.0).calculate(3300.0), 0.0)
    }
}
