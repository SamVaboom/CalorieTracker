package com.sam.caloriestreak.domain.editing

import com.sam.caloriestreak.data.local.entity.IngredientEntity

data class IngredientDraft(
    val name: String = "",
    val calories: Double = 0.0,
    val referenceAmount: Double = 100.0,
    val referenceUnit: String = "g",
    val proteinPerReferenceAmount: Double? = null,
    val category: String = "",
    val favorite: Boolean = false,
    val archived: Boolean = false
) {
    fun isValid(): Boolean = name.isNotBlank() && calories >= 0.0 &&
        referenceAmount > 0.0 && referenceUnit.isNotBlank() &&
        (proteinPerReferenceAmount == null || proteinPerReferenceAmount >= 0.0)

    fun toEntity(existing: IngredientEntity?, id: String, now: Long): IngredientEntity {
        require(isValid()) { "Invalid ingredient values" }
        return IngredientEntity(
            id = existing?.id ?: id,
            name = name.trim(),
            // Retain legacy data without exposing Brand in the current UI.
            brand = existing?.brand,
            calories = calories,
            referenceAmount = referenceAmount,
            referenceUnit = referenceUnit.trim(),
            proteinPerReferenceAmount = proteinPerReferenceAmount,
            category = category.trim().takeIf { it.isNotEmpty() },
            favorite = favorite,
            archived = archived,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
    }

    companion object {
        fun from(entity: IngredientEntity): IngredientDraft = IngredientDraft(
            name = entity.name,
            calories = entity.calories,
            referenceAmount = entity.referenceAmount,
            referenceUnit = entity.referenceUnit,
            proteinPerReferenceAmount = entity.proteinPerReferenceAmount,
            category = entity.category.orEmpty(),
            favorite = entity.favorite,
            archived = entity.archived
        )
    }
}
