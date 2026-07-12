package com.sam.caloriestreak.domain.calculation

object IngredientCalorieCalculator {
    fun calories(storedCalories: Double, referenceAmount: Double, usedAmount: Double): Double {
        if (storedCalories < 0 || referenceAmount <= 0 || usedAmount < 0) return 0.0
        return storedCalories * usedAmount / referenceAmount
    }
}

object RecipeCalorieCalculator {
    fun total(itemCalories: List<Double>): Double = itemCalories.sum().coerceAtLeast(0.0)

    fun perServing(totalCalories: Double, servings: Double): Double =
        if (servings <= 0) 0.0 else totalCalories / servings

    fun forFraction(totalCalories: Double, fraction: Double): Double =
        (totalCalories * fraction.coerceAtLeast(0.0)).coerceAtLeast(0.0)

    fun forServings(totalCalories: Double, recipeServings: Double, selectedServings: Double): Double =
        perServing(totalCalories, recipeServings) * selectedServings.coerceAtLeast(0.0)
}
