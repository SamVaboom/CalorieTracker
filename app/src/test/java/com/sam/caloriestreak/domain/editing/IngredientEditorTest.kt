package com.sam.caloriestreak.domain.editing

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientEditorTest {
    private val existing = IngredientEntity(
        id = "ingredient-1",
        name = "Mozzarella",
        brand = "Old brand",
        calories = 280.0,
        referenceAmount = 100.0,
        referenceUnit = "g",
        category = "Dairy",
        favorite = false,
        archived = false,
        createdAt = 100L,
        updatedAt = 200L
    )

    @Test
    fun existingIngredientLoadsIntoDraft() {
        val draft = IngredientDraft.from(existing)
        assertEquals("Mozzarella", draft.name)
        assertEquals("Old brand", draft.brand)
        assertEquals(280.0, draft.calories, 0.0001)
        assertEquals(100.0, draft.referenceAmount, 0.0001)
        assertEquals("g", draft.referenceUnit)
        assertEquals("Dairy", draft.category)
    }

    @Test
    fun editPreservesIdentityAndCreationTime() {
        val updated = IngredientDraft.from(existing).copy(
            name = "Light Mozzarella",
            brand = "New brand",
            calories = 250.0,
            referenceAmount = 125.0,
            referenceUnit = "g",
            category = "Cheese",
            favorite = true,
            archived = true
        ).toEntity(existing = existing, id = "ignored", now = 500L)

        assertEquals(existing.id, updated.id)
        assertEquals(existing.createdAt, updated.createdAt)
        assertEquals(500L, updated.updatedAt)
        assertEquals("Light Mozzarella", updated.name)
        assertEquals("New brand", updated.brand)
        assertEquals(250.0, updated.calories, 0.0001)
        assertEquals(125.0, updated.referenceAmount, 0.0001)
        assertEquals("Cheese", updated.category)
        assertTrue(updated.favorite)
        assertTrue(updated.archived)
    }

    @Test
    fun blankOptionalFieldsBecomeNull() {
        val updated = IngredientDraft(
            name = "Tomato",
            brand = "  ",
            calories = 20.0,
            referenceAmount = 100.0,
            referenceUnit = "g",
            category = ""
        ).toEntity(existing = null, id = "new", now = 10L)

        assertNull(updated.brand)
        assertNull(updated.category)
    }

    @Test
    fun invalidValuesAreRejected() {
        assertFalse(IngredientDraft(name = "", calories = 10.0).isValid())
        assertFalse(IngredientDraft(name = "X", calories = -1.0).isValid())
        assertFalse(IngredientDraft(name = "X", calories = 1.0, referenceAmount = 0.0).isValid())
        assertFalse(IngredientDraft(name = "X", calories = 1.0, referenceUnit = " ").isValid())
        assertTrue(IngredientDraft(name = "X", calories = 0.0, referenceAmount = 1.0, referenceUnit = "piece").isValid())
    }
}
