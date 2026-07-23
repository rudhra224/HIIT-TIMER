package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerProfileDao {
    @Query("SELECT * FROM timer_profiles ORDER BY displayOrder ASC, createdAt DESC")
    fun getAllProfiles(): Flow<List<TimerProfile>>

    @Query("SELECT * FROM timer_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): TimerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: TimerProfile): Long

    @Update
    suspend fun updateProfile(profile: TimerProfile)

    @Delete
    suspend fun deleteProfile(profile: TimerProfile)

    @Query("DELETE FROM timer_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)

    @Query("SELECT COUNT(*) FROM timer_profiles")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<TimerProfile>)
}
