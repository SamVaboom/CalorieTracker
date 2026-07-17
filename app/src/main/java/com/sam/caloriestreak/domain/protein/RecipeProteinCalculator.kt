package com.sam.caloriestreak.domain.protein

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity

object RecipeProteinCalculator {
    fun calculate(
        items: List<RecipeItemEntity>,
        ingredientsById: Map<String, IngredientEntity>
    ): RecipeProteinSummary {
        val results = items.map { item ->
            val ingredient = ingredientsById[item.ingredientId]
            val grams = ingredient?.let { IngredientProteinCalculator.grams(it, item.amount, item.unit) }
            RecipeIngredientProtein(
                ingredientId = item.ingredientId,
                knownGrams = grams,
                assigned = ingredient?.proteinPerReferenceAmount != null && grams != null
            )
        }
        return RecipeProteinSummary(
            knownGrams = results.sumOf { it.knownGrams ?: 0.0 },
            complete = results.isNotEmpty() && results.all { it.assigned },
            missingIngredientCount = results.count { !it.assigned },
            ingredients = results
        )
    }

    fun forFraction(summary: RecipeProteinSummary, multiplier: Double): ProteinSummary {
        if (multiplier < 0.0) return ProteinSummary(0.0, false, summary.missingIngredientCount, false)
        return ProteinSummary(
            knownGrams = summary.knownGrams * multiplier,
            complete = summary.complete,
            missingCount = summary.missingIngredientCount,
            hasKnownData = summary.ingredients.any { it.knownGrams != null }
        )
    }
}
