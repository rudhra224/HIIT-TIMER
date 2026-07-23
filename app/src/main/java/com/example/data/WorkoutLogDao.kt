package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_logs ORDER BY completedAtTimestamp DESC")
    fun getAllLogs(): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs ORDER BY completedAtTimestamp DESC")
    suspend fun getAllLogsList(): List<WorkoutLog>

    @Query("SELECT * FROM workout_logs WHERE completedAtTimestamp >= :startTimestamp ORDER BY completedAtTimestamp DESC")
    fun getLogsSince(startTimestamp: Long): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkoutLog): Long

    @Delete
    suspend fun deleteLog(log: WorkoutLog)

    @Query("DELETE FROM workout_logs")
    suspend fun clearAllLogs()

    @Query("SELECT SUM(durationSec) FROM workout_logs")
    fun getTotalWorkoutSecondsFlow(): Flow<Int?>

    @Query("SELECT SUM(caloriesBurned) FROM workout_logs")
    fun getTotalCaloriesBurnedFlow(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM workout_logs")
    fun getTotalWorkoutsCompletedFlow(): Flow<Int>
}
