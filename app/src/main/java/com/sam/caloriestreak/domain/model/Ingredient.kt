package com.sam.caloriestreak.domain.model

enum class IngredientUnit { GRAM, MILLILITRE, PIECE, SLICE, TABLESPOON, TEASPOON, CUP, CUSTOM }

data class Ingredient(
    val id: String,
    val name: String,
    val brand: String? = null,
    val calories: Double,
    val referenceAmount: Double,
    val referenceUnit: IngredientUnit,
    val favorite: Boolean = false,
    val archived: Boolean = false
) {
    fun caloriesFor(amount: Double): Double =
        if (referenceAmount <= 0.0) 0.0 else calories * amount / referenceAmount
}
