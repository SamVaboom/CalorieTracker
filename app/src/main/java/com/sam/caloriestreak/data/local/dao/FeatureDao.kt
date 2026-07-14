package com.sam.caloriestreak.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sam.caloriestreak.data.local.entity.ActivityEventEntity
import com.sam.caloriestreak.data.local.entity.EarnedAchievementEntity
import com.sam.caloriestreak.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeatureDao {
    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC")
    fun observeWeights(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries ORDER BY timestamp")
    suspend fun allWeights(): List<WeightEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeight(entry: WeightEntryEntity)

    @Update suspend fun updateWeight(entry: WeightEntryEntity)
    @Delete suspend fun deleteWeight(entry: WeightEntryEntity)

    @Query("SELECT * FROM earned_achievements ORDER BY earnedAt DESC")
    fun observeEarnedAchievements(): Flow<List<EarnedAchievementEntity>>

    @Query("SELECT achievementId FROM earned_achievements")
    suspend fun earnedAchievementIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEarnedAchievement(entry: EarnedAchievementEntity): Long

    @Query("DELETE FROM earned_achievements WHERE achievementId = :achievementId")
    suspend fun deleteEarnedAchievement(achievementId: String)

    @Query("UPDATE earned_achievements SET seen = 1")
    suspend fun markAllAchievementsSeen()

    @Query("SELECT * FROM activity_events ORDER BY timestamp")
    fun observeActivityEvents(): Flow<List<ActivityEventEntity>>

    @Query("SELECT * FROM activity_events ORDER BY timestamp")
    suspend fun allActivityEvents(): List<ActivityEventEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertActivityEvent(event: ActivityEventEntity): Long
}
