package com.sam.caloriestreak.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sam.caloriestreak.data.local.database.CalorieStreakDatabase
import com.sam.caloriestreak.data.local.database.DatabaseProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomMigrationTest {
    private val databaseName = "room-migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CalorieStreakDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrateVersion1To5_preservesIngredientAndValidatesEverySchema() {
        helper.createDatabase(databaseName, 1).apply {
            execSQL(
                """
                INSERT INTO ingredients (
                    id, name, brand, calories, referenceAmount, referenceUnit,
                    category, favorite, archived, createdAt, updatedAt
                ) VALUES ('ingredient-1', 'Mozzarella', 'Legacy brand', 280.0, 100.0, 'g',
                    'Dairy', 1, 0, 10, 11)
                """.trimIndent()
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            5,
            true,
            *DatabaseProvider.ALL_MIGRATIONS
        )

        migrated.query("SELECT name, calories, brand FROM ingredients WHERE id = 'ingredient-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Mozzarella", cursor.getString(0))
            assertEquals(280.0, cursor.getDouble(1), 0.0001)
            assertEquals("Legacy brand", cursor.getString(2))
        }
        migrated.close()
    }

    @Test
    fun migrateVersion2To5_preservesRecipesMealsCaloriesAndDailyState() {
        helper.createDatabase(databaseName, 2).apply {
            seedVersion2Data(this)
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            5,
            true,
            DatabaseProvider.MIGRATION_2_3,
            DatabaseProvider.MIGRATION_3_4,
            DatabaseProvider.MIGRATION_4_5
        )

        assertSingleValue(migrated, "SELECT COUNT(*) FROM ingredients", 1L)
        assertSingleValue(migrated, "SELECT COUNT(*) FROM recipes", 1L)
        assertSingleValue(migrated, "SELECT COUNT(*) FROM recipe_items", 1L)
        assertSingleValue(migrated, "SELECT COUNT(*) FROM meal_logs", 1L)
        assertSingleValue(migrated, "SELECT COUNT(*) FROM grocery_items", 1L)
        assertSingleValue(migrated, "SELECT COUNT(*) FROM daily_logs", 1L)
        migrated.query("SELECT calories FROM meal_logs WHERE id = 'meal-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(420.5, cursor.getDouble(0), 0.0001)
        }
        migrated.query("SELECT totalCalories, score, freezeUsed FROM daily_logs WHERE dateEpochDay = 20000").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(420.5, cursor.getDouble(0), 0.0001)
            assertEquals(80.0, cursor.getDouble(1), 0.0001)
            assertEquals(1, cursor.getInt(2))
        }
        migrated.close()
    }

    @Test
    fun migrateVersion4To5_preservesWeightAchievementsAndActivity() {
        helper.createDatabase(databaseName, 4).apply {
            execSQL("INSERT INTO weight_entries VALUES ('weight-1', 94.2, 1000, 'note', 1000, 1000)")
            execSQL("INSERT INTO earned_achievements VALUES ('earned-1', 'bullseye', 1000, 20000, 100.0, 0)")
            execSQL("INSERT INTO activity_events VALUES ('event-1', 'APP_OPEN', 20000, 1000, NULL)")
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            5,
            true,
            DatabaseProvider.MIGRATION_4_5
        )

        assertSingleValue(migrated, "SELECT COUNT(*) FROM weight_entries", 1L)
        assertSingleValue(migrated, "SELECT COUNT(*) FROM activity_events", 1L)
        migrated.query("SELECT popupDismissed, popupSuppressed, unlockSource FROM earned_achievements WHERE id = 'earned-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
            assertEquals(0, cursor.getInt(1))
            assertEquals("LEGACY", cursor.getString(2))
        }
        migrated.close()
    }

    private fun seedVersion2Data(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO ingredients VALUES ('ingredient-1', 'Cheese', 'Brand', 280.0, 100.0, 'g', 'Dairy', 1, 0, 10, 10)")
        db.execSQL("INSERT INTO recipes VALUES ('recipe-1', 'Pizza', 'Dinner', 2.0, 1, 0, 10, 10)")
        db.execSQL("INSERT INTO recipe_items VALUES ('item-1', 'recipe-1', 'ingredient-1', 'Cheese', 150.0, 'g', NULL)")
        db.execSQL("INSERT INTO meal_logs VALUES ('meal-1', 20000, 1000, 'recipe-1', 'Pizza', '1 serving', 0.5, 420.5, NULL, 1000, 1000)")
        db.execSQL("INSERT INTO grocery_items VALUES ('grocery-1', 'ingredient-1', 'Cheese', 150.0, 'g', 0, 0, 1000)")
        db.execSQL("INSERT INTO daily_logs VALUES (20000, 420.5, 80.0, 1, 1, 1, 0, 0, 1000, 1000)")
    }

    private fun assertSingleValue(db: SupportSQLiteDatabase, sql: String, expected: Long) {
        db.query(sql).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(expected, cursor.getLong(0))
        }
    }
}
