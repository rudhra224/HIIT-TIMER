package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_profiles")
data class TimerProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val workDurationSec: Int,
    val restDurationSec: Int,
    val setsCount: Int,
    val warmupDurationSec: Int = 10,
    val cooldownDurationSec: Int = 10,
    val preStartDelaySec: Int = 5,
    val enableAudio: Boolean = true,
    val enableHaptics: Boolean = true,
    val accentColorHex: String = "#FF5722",
    val isCustom: Boolean = true,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun calculateTotalDurationSec(): Int {
        return warmupDurationSec + (setsCount * (workDurationSec + restDurationSec)) + cooldownDurationSec
    }

    fun formattedTotalTime(): String {
        val totalSec = calculateTotalDurationSec()
        val mins = totalSec / 60
        val secs = totalSec % 60
        return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }
}
