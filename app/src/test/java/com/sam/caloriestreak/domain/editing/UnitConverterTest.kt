package com.sam.caloriestreak.domain.editing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitConverterTest {
    @Test
    fun convertsMassAndVolumeUnits() {
        assertEquals(500.0, UnitConverter.convert(0.5, "kg", "g")!!, 0.0001)
        assertEquals(1.5, UnitConverter.convert(1500.0, "ml", "l")!!, 0.0001)
    }

    @Test
    fun acceptsEquivalentCountUnits() {
        assertEquals(3.0, UnitConverter.convert(3.0, "pieces", "piece")!!, 0.0001)
        assertTrue(UnitConverter.areCompatible("pcs", "unit"))
    }

    @Test
    fun rejectsIncompatibleUnits() {
        assertNull(UnitConverter.convert(100.0, "g", "ml"))
        assertFalse(UnitConverter.areCompatible("piece", "g"))
    }

    @Test
    fun preservesUnknownButIdenticalUnits() {
        assertEquals(2.0, UnitConverter.convert(2.0, "slice", "slice")!!, 0.0001)
    }
}
