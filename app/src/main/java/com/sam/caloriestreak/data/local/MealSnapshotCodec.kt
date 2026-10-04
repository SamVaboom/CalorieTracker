package com.sam.caloriestreak.data.local

import com.sam.caloriestreak.domain.calculation.MealIngredientSnapshot
import org.json.JSONArray
import org.json.JSONObject

object MealSnapshotCodec {
    fun encode(items: List<MealIngredientSnapshot>): String = JSONArray().apply {
        items.forEach { item ->
            put(JSONObject().apply {
                put("ingredientId", item.ingredientId)
                put("name", item.name)
                put("savedAmount", item.savedAmount)
                put("multiplier", item.multiplier)
                put("amount", item.amount)
                put("unit", item.unit)
                put("calories", item.calories)
                put("proteinGrams", item.proteinGrams ?: JSONObject.NULL)
                put("note", item.note ?: JSONObject.NULL)
            })
        }
    }.toString()

    fun decode(value: String): List<MealIngredientSnapshot> {
        val array = JSONArray(value)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            MealIngredientSnapshot(
                item.getString("ingredientId"), item.getString("name"), item.getDouble("savedAmount"),
                item.getDouble("multiplier"), item.getString("unit"), item.getDouble("calories"),
                if (item.isNull("proteinGrams")) null else item.getDouble("proteinGrams"),
                if (item.isNull("note")) null else item.getString("note")
            )
        }
    }
}
