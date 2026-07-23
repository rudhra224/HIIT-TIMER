package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val dailyGoalMinutes: Int = 20,
    val notificationTimeMinuteOfDay: Int = 1200, // 20:00 (8 PM)
    val enableDailyReminder: Boolean = false,
    val userWeightKg: Int = 70,
    val themeMode: String = "DARK", // "SYSTEM", "DARK", "LIGHT"
    val accentColorHex: String = "#D0BCFF",
    val cloudSyncEnabled: Boolean = false,
    val lastSyncedAt: Long = 0L
)
