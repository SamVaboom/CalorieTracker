package com.sam.caloriestreak.domain.calculation

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.domain.editing.UnitConverter
import com.sam.caloriestreak.domain.protein.IngredientProteinCalculator
import com.sam.caloriestreak.domain.protein.ProteinSummary

data class MealIngredientSnapshot(
    val ingredientId: String,
    val name: String,
    val savedAmount: Double,
    val multiplier: Double,
    val unit: String,
    val calories: Double,
    val proteinGrams: Double?,
    val note: String? = null
) {
    val amount: Double get() = savedAmount * multiplier
}

data class FlexibleMealConfiguration(val ingredients: List<MealIngredientSnapshot>) {
    val calories: Double get() = ingredients.sumOf { it.calories }
    val protein: ProteinSummary get() = ProteinSummary(
        knownGrams = ingredients.sumOf { it.proteinGrams ?: 0.0 },
        complete = ingredients.isNotEmpty() && ingredients.all { it.proteinGrams != null },
        missingCount = ingredients.count { it.proteinGrams == null },
        hasKnownData = ingredients.any { it.proteinGrams != null }
    )
}

object FlexibleMealCalculator {
    fun configure(
        items: List<RecipeItemEntity>,
        ingredients: List<IngredientEntity>,
        multipliers: Map<String, Double>
    ): FlexibleMealConfiguration {
        require(multipliers.values.all { it.isFinite() && it >= 0.0 })
        require(multipliers.keys.all { id -> items.any { it.id == id } })
        val byId = ingredients.associateBy { it.id }
        return FlexibleMealConfiguration(items.mapNotNull { item ->
            val multiplier = multipliers[item.id] ?: 0.0
            if (multiplier == 0.0) return@mapNotNull null
            val ingredient = requireNotNull(byId[item.ingredientId]) { "Ingredient no longer available" }
            val amount = item.amount * multiplier
            require(amount.isFinite() && amount > 0.0)
            val converted = requireNotNull(UnitConverter.convert(amount, item.unit, ingredient.referenceUnit))
            val calories = IngredientCalorieCalculator.calories(ingredient.calories, ingredient.referenceAmount, converted)
            val protein = IngredientProteinCalculator.grams(ingredient, amount, item.unit)
            require(calories.isFinite() && (protein == null || protein.isFinite()))
            MealIngredientSnapshot(ingredient.id, ingredient.name, item.amount, multiplier, item.unit, calories, protein, item.note)
        }).also { require(it.calories.isFinite() && it.protein.knownGrams.isFinite()) }
    }
}
