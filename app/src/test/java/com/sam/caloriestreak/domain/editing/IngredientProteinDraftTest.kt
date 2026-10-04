package com.sam.caloriestreak.domain.editing

import com.sam.caloriestreak.data.local.entity.IngredientEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientProteinDraftTest {
    private fun draft(protein: Double?) = IngredientDraft(
        name = "Mozzarella",
        calories = 280.0,
        referenceAmount = 100.0,
        referenceUnit = "g",
        proteinPerReferenceAmount = protein
    )

    @Test fun optionalProteinMayRemainUnknown() {
        val entity = draft(null).toEntity(null, "id", 1)
        assertTrue(draft(null).isValid())
        assertNull(entity.proteinPerReferenceAmount)
    }

    @Test fun explicitZeroProteinRemainsAssignedZero() {
        val entity = draft(0.0).toEntity(null, "id", 1)
        assertTrue(draft(0.0).isValid())
        assertEquals(0.0, entity.proteinPerReferenceAmount!!, 0.0001)
    }

    @Test fun decimalProteinIsStoredAndNegativeProteinIsRejected() {
        assertEquals(22.4, draft(22.4).toEntity(null, "id", 1).proteinPerReferenceAmount!!, 0.0001)
        assertFalse(draft(-0.1).isValid())
    }

    @Test fun clearingProteinReturnsToUnknownWithoutChangingLegacyBrand() {
        val existing = IngredientEntity(
            id = "id",
            name = "Mozzarella",
            brand = "Legacy Brand",
            calories = 280.0,
            referenceAmount = 100.0,
            referenceUnit = "g",
            proteinPerReferenceAmount = 22.0,
            createdAt = 1,
            updatedAt = 1
        )
        val updated = IngredientDraft.from(existing).copy(proteinPerReferenceAmount = null).toEntity(existing, "ignored", 2)
        assertNull(updated.proteinPerReferenceAmount)
        assertEquals("Legacy Brand", updated.brand)
    }
}
