package com.sam.caloriestreak.domain.editing

object UnitConverter {
    private data class UnitDefinition(val dimension: String, val toBase: Double)

    private val definitions = mapOf(
        "mg" to UnitDefinition("mass", 0.001),
        "g" to UnitDefinition("mass", 1.0),
        "kg" to UnitDefinition("mass", 1000.0),
        "ml" to UnitDefinition("volume", 1.0),
        "l" to UnitDefinition("volume", 1000.0),
        "piece" to UnitDefinition("count", 1.0),
        "pieces" to UnitDefinition("count", 1.0),
        "pc" to UnitDefinition("count", 1.0),
        "pcs" to UnitDefinition("count", 1.0),
        "unit" to UnitDefinition("count", 1.0),
        "units" to UnitDefinition("count", 1.0)
    )

    private fun normalize(unit: String): String = unit.trim().lowercase()

    fun convert(amount: Double, fromUnit: String, toUnit: String): Double? {
        if (amount < 0.0) return null
        val from = normalize(fromUnit)
        val to = normalize(toUnit)
        if (from.isBlank() || to.isBlank()) return null
        if (from == to) return amount

        val fromDefinition = definitions[from] ?: return null
        val toDefinition = definitions[to] ?: return null
        if (fromDefinition.dimension != toDefinition.dimension) return null
        return amount * fromDefinition.toBase / toDefinition.toBase
    }

    fun areCompatible(first: String, second: String): Boolean = convert(1.0, first, second) != null
}
