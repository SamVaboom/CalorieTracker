package com.sam.caloriestreak.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS recipes (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, description TEXT, servings REAL NOT NULL, favorite INTEGER NOT NULL, archived INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS recipe_items (id TEXT NOT NULL PRIMARY KEY, recipeId TEXT NOT NULL, ingredientId TEXT NOT NULL, ingredientName TEXT NOT NULL, amount REAL NOT NULL, unit TEXT NOT NULL, note TEXT)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_recipe_items_recipeId ON recipe_items(recipeId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_recipe_items_ingredientId ON recipe_items(ingredientId)")
            db.execSQL("CREATE TABLE IF NOT EXISTS meal_logs (id TEXT NOT NULL PRIMARY KEY, dateEpochDay INTEGER NOT NULL, timeMillis INTEGER NOT NULL, recipeId TEXT, recipeName TEXT NOT NULL, portionDescription TEXT NOT NULL, portionMultiplier REAL NOT NULL, calories REAL NOT NULL, note TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_meal_logs_dateEpochDay ON meal_logs(dateEpochDay)")
            db.execSQL("CREATE TABLE IF NOT EXISTS grocery_items (id TEXT NOT NULL PRIMARY KEY, ingredientId TEXT, name TEXT NOT NULL, amount REAL NOT NULL, unit TEXT NOT NULL, checked INTEGER NOT NULL, manuallyAdded INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS daily_logs (dateEpochDay INTEGER NOT NULL PRIMARY KEY, totalCalories REAL NOT NULL, score REAL NOT NULL, finalized INTEGER NOT NULL, streakSuccessful INTEGER NOT NULL, freezeUsed INTEGER NOT NULL, manualCheatDay INTEGER NOT NULL, freezeQualifying INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE daily_logs ADD COLUMN targetCalories REAL NOT NULL DEFAULT 1650.0")
            db.execSQL("ALTER TABLE daily_logs ADD COLUMN scoreCurveVersion INTEGER NOT NULL DEFAULT 1")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_logs_dateEpochDay ON daily_logs(dateEpochDay)")
            db.execSQL("CREATE TABLE IF NOT EXISTS weight_entries (id TEXT NOT NULL PRIMARY KEY, kilograms REAL NOT NULL, timestamp INTEGER NOT NULL, note TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_weight_entries_timestamp ON weight_entries(timestamp)")
            db.execSQL("CREATE TABLE IF NOT EXISTS earned_achievements (id TEXT NOT NULL PRIMARY KEY, achievementId TEXT NOT NULL, earnedAt INTEGER NOT NULL, triggeringEpochDay INTEGER, progressAtUnlock REAL, seen INTEGER NOT NULL)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_earned_achievements_achievementId ON earned_achievements(achievementId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_earned_achievements_earnedAt ON earned_achievements(earnedAt)")
        }
    }

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS activity_events (id TEXT NOT NULL PRIMARY KEY, type TEXT NOT NULL, epochDay INTEGER NOT NULL, timestamp INTEGER NOT NULL, metadata TEXT)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_activity_events_type ON activity_events(type)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_activity_events_epochDay ON activity_events(epochDay)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_activity_events_timestamp ON activity_events(timestamp)")
        }
    }

    @Volatile private var instance: CalorieStreakDatabase? = null

    fun get(context: Context): CalorieStreakDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(context.applicationContext, CalorieStreakDatabase::class.java, "calorie_streak.db")
            .addMigrations(migration1To2, migration2To3, migration3To4)
            .build()
            .also { instance = it }
    }
}
