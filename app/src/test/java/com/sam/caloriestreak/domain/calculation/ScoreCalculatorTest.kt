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

    @Test fun exactTargetReturnsOneHundredInternally() {
        assertEquals(100.0, calculator.calculate(1650.0), 0.0)
    }

    @Test fun tinyNoiseAroundTargetReturnsOneHundred() {
        assertEquals(100.0, calculator.calculate(1650.0 - 0.0000001), 0.0)
        assertEquals(100.0, calculator.calculate(1650.0 + 0.0000001), 0.0)
    }

    @Test fun displayUsesMathematicalRounding() {
        assertEquals(100, ScoreDisplay.percent(99.6))
        assertEquals(99, ScoreDisplay.percent(99.2))
        assertEquals(85, ScoreDisplay.percent(84.5))
        assertEquals(84, ScoreDisplay.percent(84.4))
    }

    @Test fun genuineNinetyNineRemainsNinetyNine() {
        assertEquals(99, ScoreDisplay.percent(99.2))
    }

    @Test fun interpolatesBetweenPoints() {
        assertEquals(60.0, calculator.calculate(1300.0), 0.0001)
        assertEquals(90.0, calculator.calculate(1525.0), 0.0001)
    }

    @Test fun aboveTargetDescends() {
        assertTrue(calculator.calculate(1800.0) < calculator.calculate(1650.0))
        assertEquals(75.0, calculator.calculate(1800.0), 0.0001)
    }

    @Test fun clampsOutsideCurve() {
        assertEquals(0.0, calculator.calculate(500.0), 0.0001)
        assertEquals(0.0, calculator.calculate(2500.0), 0.0001)
    }

    @Test fun scoreAlwaysStaysWithinBounds() {
        listOf(-1000.0, 0.0, 1650.0, 100000.0).forEach { calories ->
            val score = calculator.calculate(calories)
            assertTrue(score in 0.0..100.0)
        }
    }

    @Test fun supportsDecimalCalories() {
        assertEquals(60.1, calculator.calculate(1300.5), 0.0001)
    }
}
