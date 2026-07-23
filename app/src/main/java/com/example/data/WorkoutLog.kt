package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val routineName: String,
    val durationSec: Int,
    val caloriesBurned: Int,
    val roundsCompleted: Int,
    val totalRounds: Int,
    val completedAtTimestamp: Long = System.currentTimeMillis()
)
