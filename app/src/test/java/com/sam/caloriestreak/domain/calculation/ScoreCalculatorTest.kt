package com.sam.caloriestreak.domain.calculation

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreCalculatorTest {
    private val calculator = ScoreCalculator()

    @Test fun exactPoints() {
        ScoreCalculator.defaultPoints.forEach { point ->
            assertEquals(point.score, calculator.calculate(point.calories), 0.0001)
        }
    }

    @Test fun interpolatesBetweenPoints() {
        assertEquals(60.0, calculator.calculate(1300.0), 0.0001)
        assertEquals(90.0, calculator.calculate(1525.0), 0.0001)
    }

    @Test fun clampsOutsideCurve() {
        assertEquals(0.0, calculator.calculate(500.0), 0.0001)
        assertEquals(0.0, calculator.calculate(2500.0), 0.0001)
    }

    @Test fun supportsDecimalCalories() {
        assertEquals(60.1, calculator.calculate(1300.5), 0.0001)
    }
}
