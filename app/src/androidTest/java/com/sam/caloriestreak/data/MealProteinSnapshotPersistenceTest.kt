package com.sam.caloriestreak.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sam.caloriestreak.data.local.database.CalorieStreakDatabase
import com.sam.caloriestreak.data.local.entity.IngredientEntity
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.data.local.entity.RecipeEntity
import com.sam.caloriestreak.data.local.entity.RecipeItemEntity
import com.sam.caloriestreak.domain.protein.DailyProteinCalculator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealProteinSnapshotPersistenceTest {
    @get:Rule val testName = TestName()
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var database: CalorieStreakDatabase

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, CalorieStreakDatabase::class.java).build()
    }

    private fun meal(id: String, protein: Double?, complete: Boolean) = MealLogEntity(
        id = id,
        dateEpochDay = 20,
        timeMillis = id.hashCode().toLong(),
        recipeId = if (id.startsWith("recipe")) "recipe" else null,
        recipeName = id,
        portionDescription = "1 serving",
        portionMultiplier = 1.0,
        calories = 500.0,
        proteinGramsSnapshot = protein,
        proteinDataComplete = complete,
        missingProteinItemCount = if (complete) 0 else 1,
        createdAt = 1,
        updatedAt = 1
    )

    @Test fun ingredientAndRecipeEditsDoNotChangeOldMealProtein() = runBlocking {
        val ingredient = IngredientEntity(
            id = "ingredient",
            name = "Cheese",
            calories = 280.0,
            referenceAmount = 100.0,
            referenceUnit = "g",
            proteinPerReferenceAmount = 22.0,
            createdAt = 1,
            updatedAt = 1
        )
        database.ingredientDao().upsert(ingredient)
        database.appDao().replaceRecipe(
            RecipeEntity("recipe", "Pizza", servings = 2.0, createdAt = 1, updatedAt = 1),
            listOf(RecipeItemEntity("item", "recipe", "ingredient", "Cheese", 300.0, "g"))
        )
        database.appDao().upsertMeal(meal("recipe-old", 33.0, true))

        database.ingredientDao().upsert(ingredient.copy(proteinPerReferenceAmount = 30.0, updatedAt = 2))
        database.appDao().replaceRecipe(
            RecipeEntity("recipe", "Changed Pizza", servings = 3.0, createdAt = 1, updatedAt = 2),
            listOf(RecipeItemEntity("item-new", "recipe", "ingredient", "Cheese", 500.0, "g"))
        )

        val historical = database.appDao().allMeals().single()
        assertEquals(33.0, historical.proteinGramsSnapshot!!, 0.0001)
        assertEquals(500.0, historical.calories, 0.0001)

        database.appDao().upsertMeal(meal("recipe-new", 50.0, true))
        assertEquals(listOf(33.0, 50.0), database.appDao().allMeals().mapNotNull { it.proteinGramsSnapshot })
    }

    @Test fun manualUnknownAndExplicitZeroRemainDistinct() = runBlocking {
        database.appDao().upsertMeal(meal("manual-unknown", null, false))
        database.appDao().upsertMeal(meal("manual-zero", 0.0, true))
        val meals = database.appDao().allMeals().associateBy { it.id }
        assertNull(meals.getValue("manual-unknown").proteinGramsSnapshot)
        assertEquals(0.0, meals.getValue("manual-zero").proteinGramsSnapshot!!, 0.0001)
    }

    @Test fun deletingMealUpdatesDerivedDailyProtein() = runBlocking {
        val first = meal("manual-first", 40.0, true)
        val second = meal("manual-second", 60.0, true)
        database.appDao().upsertMeal(first)
        database.appDao().upsertMeal(second)
        assertEquals(100.0, DailyProteinCalculator.calculate(database.appDao().allMeals()).knownGrams, 0.0001)
        database.appDao().deleteMeal(second)
        assertEquals(40.0, DailyProteinCalculator.calculate(database.appDao().allMeals()).knownGrams, 0.0001)
    }
}
