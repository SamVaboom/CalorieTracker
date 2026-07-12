package com.sam.caloriestreak.domain.calculation

import org.junit.Assert.assertEquals
import org.junit.Test

class CalorieCalculatorsTest {
    @Test fun ingredientAmountCalculation() {
        assertEquals(420.0, IngredientCalorieCalculator.calories(280.0, 100.0, 150.0), 0.001)
        assertEquals(52.5, IngredientCalorieCalculator.calories(105.0, 1.0, 0.5), 0.001)
    }

    @Test fun recipePortions() {
        assertEquals(900.0, RecipeCalorieCalculator.forFraction(1800.0, 0.5), 0.001)
        assertEquals(450.0, RecipeCalorieCalculator.perServing(1800.0, 4.0), 0.001)
        assertEquals(900.0, RecipeCalorieCalculator.forServings(1800.0, 4.0, 2.0), 0.001)
    }

    @Test fun invalidAmountsReturnZero() {
        assertEquals(0.0, IngredientCalorieCalculator.calories(280.0, 0.0, 150.0), 0.001)
        assertEquals(0.0, RecipeCalorieCalculator.perServing(1800.0, 0.0), 0.001)
    }
}
