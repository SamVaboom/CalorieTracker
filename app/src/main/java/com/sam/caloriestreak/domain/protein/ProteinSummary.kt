package com.sam.caloriestreak.domain.protein

data class ProteinSummary(
    val knownGrams: Double,
    val complete: Boolean,
    val missingCount: Int,
    val hasKnownData: Boolean
) {
    val exactGramsOrNull: Double? get() = knownGrams.takeIf { complete }
}

data class RecipeIngredientProtein(
    val ingredientId: String,
    val knownGrams: Double?,
    val assigned: Boolean
)

data class RecipeProteinSummary(
    val knownGrams: Double,
    val complete: Boolean,
    val missingIngredientCount: Int,
    val ingredients: List<RecipeIngredientProtein>
) {
    fun perServing(servings: Double): Double? =
        if (complete && servings > 0.0) knownGrams / servings else null
}
