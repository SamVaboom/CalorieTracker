package com.sam.caloriestreak.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sam.caloriestreak.data.local.database.CalorieStreakDatabase
import com.sam.caloriestreak.data.local.entity.AchievementPopupSummaryEntity
import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AchievementPopupPersistenceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "achievement-popup-test.db"
    private lateinit var database: CalorieStreakDatabase

    @Before
    fun setUp() {
        context.deleteDatabase(databaseName)
        database = openDatabase()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun dismissingPopupPersistsAcrossDatabaseReopen() = runBlocking {
        val record = EarnedAchievementEntity(
            id = "earned-1",
            achievementId = "bullseye",
            earnedAt = 1L,
            triggeringEpochDay = 1L,
            progressAtUnlock = 100.0,
            popupDismissed = false
        )
        database.featureDao().insertEarnedAchievement(record)
        assertEquals(listOf("earned-1"), database.featureDao().observePendingAchievementPopups().first().map { it.id })

        database.featureDao().dismissAchievementPopup(record.id)
        assertTrue(database.featureDao().observePendingAchievementPopups().first().isEmpty())

        database.close()
        database = openDatabase()
        assertTrue(database.featureDao().observePendingAchievementPopups().first().isEmpty())
        assertEquals(true, database.featureDao().observeEarnedAchievements().first().single().popupDismissed)
    }

    @Test
    fun retroactiveSummaryPersistsUntilDismissed() = runBlocking {
        val summary = AchievementPopupSummaryEntity("summary-1", 12, 1L)
        database.featureDao().insertPopupSummary(summary)
        assertEquals("summary-1", database.featureDao().observePendingPopupSummary().first()?.id)

        database.close()
        database = openDatabase()
        assertEquals("summary-1", database.featureDao().observePendingPopupSummary().first()?.id)

        database.featureDao().dismissPopupSummary(summary.id)
        assertEquals(null, database.featureDao().observePendingPopupSummary().first())
    }

    private fun openDatabase(): CalorieStreakDatabase = Room.databaseBuilder(
        context,
        CalorieStreakDatabase::class.java,
        databaseName
    ).build()
}
