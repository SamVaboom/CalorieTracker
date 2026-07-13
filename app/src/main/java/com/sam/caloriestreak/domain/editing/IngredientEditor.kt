package com.sam.caloriestreak.domain.editing

import com.sam.caloriestreak.data.local.entity.IngredientEntity

data class IngredientDraft(
    val name: String = "",
    val brand: String = "",
    val calories: Double = 0.0,
    val referenceAmount: Double = 100.0,
    val referenceUnit: String = "g",
    val category: String = "",
    val favorite: Boolean = false,
    val archived: Boolean = false
) {
    fun isValid(): Boolean = name.isNotBlank() && calories >= 0.0 &&
        referenceAmount > 0.0 && referenceUnit.isNotBlank()

    fun toEntity(existing: IngredientEntity?, id: String, now: Long): IngredientEntity {
        require(isValid()) { "Invalid ingredient values" }
        return IngredientEntity(
            id = existing?.id ?: id,
            name = name.trim(),
            brand = brand.trim().takeIf { it.isNotEmpty() },
            calories = calories,
            referenceAmount = referenceAmount,
            referenceUnit = referenceUnit.trim(),
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
            brand = entity.brand.orEmpty(),
            calories = entity.calories,
            referenceAmount = entity.referenceAmount,
            referenceUnit = entity.referenceUnit,
            category = entity.category.orEmpty(),
            favorite = entity.favorite,
            archived = entity.archived
        )
    }
}
