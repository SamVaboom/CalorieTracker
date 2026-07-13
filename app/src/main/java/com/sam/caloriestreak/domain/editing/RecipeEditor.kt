package com.sam.caloriestreak.domain.editing

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.domain.calculation.IngredientCalorieCalculator

data class RecipeIngredientDraft(
    val ingredientId: String,
    val amount: Double,
    val unit: String,
    val note: String = ""
)

data class RecipeDraft(
    val name: String = "",
    val description: String = "",
    val servings: Double = 2.0,
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val items: List<RecipeIngredientDraft> = emptyList()
) {
    fun isValid(ingredients: List<IngredientEntity>): Boolean {
        if (name.isBlank() || servings <= 0.0 || items.isEmpty()) return false
        val ingredientMap = ingredients.associateBy { it.id }
        if (items.map { it.ingredientId }.distinct().size != items.size) return false
        return items.all { item ->
            val ingredient = ingredientMap[item.ingredientId]
            ingredient != null && item.amount > 0.0 && item.unit.isNotBlank() &&
                UnitConverter.areCompatible(item.unit, ingredient.referenceUnit)
        }
    }

    fun totalCalories(ingredients: List<IngredientEntity>): Double {
        val ingredientMap = ingredients.associateBy { it.id }
        return items.sumOf { item ->
            val ingredient = ingredientMap[item.ingredientId] ?: return@sumOf 0.0
            val referenceAmount = UnitConverter.convert(
                amount = item.amount,
                fromUnit = item.unit,
                toUnit = ingredient.referenceUnit
            ) ?: return@sumOf 0.0
            IngredientCalorieCalculator.calories(
                storedCalories = ingredient.calories,
                referenceAmount = ingredient.referenceAmount,
                usedAmount = referenceAmount
            )
        }
    }

    fun toEntity(existing: RecipeEntity?, id: String, now: Long): RecipeEntity {
        return RecipeEntity(
            id = existing?.id ?: id,
            name = name.trim(),
            description = description.trim().takeIf { it.isNotEmpty() },
            servings = servings,
            favorite = favorite,
            archived = archived,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
    }

    fun toItems(
        recipeId: String,
        ingredients: List<IngredientEntity>,
        existingItems: List<RecipeItemEntity>,
        idFactory: () -> String
    ): List<RecipeItemEntity> {
        val ingredientMap = ingredients.associateBy { it.id }
        val existingByIngredient = existingItems.associateBy { it.ingredientId }
        return items.map { draft ->
            val ingredient = requireNotNull(ingredientMap[draft.ingredientId])
            RecipeItemEntity(
                id = existingByIngredient[draft.ingredientId]?.id ?: idFactory(),
                recipeId = recipeId,
                ingredientId = ingredient.id,
                ingredientName = ingredient.name,
                amount = draft.amount,
                unit = draft.unit.trim(),
                note = draft.note.trim().takeIf { it.isNotEmpty() }
            )
        }
    }

    companion object {
        fun from(recipe: RecipeEntity, items: List<RecipeItemEntity>): RecipeDraft = RecipeDraft(
            name = recipe.name,
            description = recipe.description.orEmpty(),
            servings = recipe.servings,
            favorite = recipe.favorite,
            archived = recipe.archived,
            items = items.map {
                RecipeIngredientDraft(
                    ingredientId = it.ingredientId,
                    amount = it.amount,
                    unit = it.unit,
                    note = it.note.orEmpty()
                )
            }
        )
    }
}
