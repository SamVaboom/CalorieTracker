package com.sam.caloriestreak.domain.protein

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.domain.editing.UnitConverter

object IngredientProteinCalculator {
    fun grams(
        proteinPerReferenceAmount: Double?,
        referenceAmount: Double,
        quantityInReferenceUnit: Double
    ): Double? {
        if (proteinPerReferenceAmount == null) return null
        if (proteinPerReferenceAmount < 0.0 || referenceAmount <= 0.0 || quantityInReferenceUnit < 0.0) return null
        return proteinPerReferenceAmount * quantityInReferenceUnit / referenceAmount
    }

    fun grams(ingredient: IngredientEntity, quantity: Double, unit: String): Double? {
        val converted = UnitConverter.convert(quantity, unit, ingredient.referenceUnit) ?: return null
        return grams(
            proteinPerReferenceAmount = ingredient.proteinPerReferenceAmount,
            referenceAmount = ingredient.referenceAmount,
            quantityInReferenceUnit = converted
        )
    }
}
