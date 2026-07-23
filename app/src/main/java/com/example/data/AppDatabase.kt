package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TimerProfile::class, WorkoutLog::class, UserSettings::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timerProfileDao(): TimerProfileDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hiit_timer_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(db: AppDatabase) {
                val defaultProfiles = listOf(
                    TimerProfile(
                        name = "Tabata Standard",
                        workDurationSec = 20,
                        restDurationSec = 10,
                        setsCount = 8,
                        warmupDurationSec = 10,
                        cooldownDurationSec = 10,
                        preStartDelaySec = 5,
                        accentColorHex = "#FF5722",
                        isCustom = false,
                        displayOrder = 1
                    ),
                    TimerProfile(
                        name = "Sprint Intervals",
                        workDurationSec = 30,
                        restDurationSec = 30,
                        setsCount = 10,
                        warmupDurationSec = 15,
                        cooldownDurationSec = 15,
                        preStartDelaySec = 5,
                        accentColorHex = "#00E676",
                        isCustom = false,
                        displayOrder = 2
                    ),
                    TimerProfile(
                        name = "Abs & Core Blast",
                        workDurationSec = 40,
                        restDurationSec = 20,
                        setsCount = 6,
                        warmupDurationSec = 10,
                        cooldownDurationSec = 10,
                        preStartDelaySec = 5,
                        accentColorHex = "#00E5FF",
                        isCustom = false,
                        displayOrder = 3
                    ),
                    TimerProfile(
                        name = "30/15 Shred",
                        workDurationSec = 30,
                        restDurationSec = 15,
                        setsCount = 12,
                        warmupDurationSec = 15,
                        cooldownDurationSec = 15,
                        preStartDelaySec = 5,
                        accentColorHex = "#D500F9",
                        isCustom = false,
                        displayOrder = 4
                    ),
                    TimerProfile(
                        name = "Full Body HIIT",
                        workDurationSec = 45,
                        restDurationSec = 15,
                        setsCount = 8,
                        warmupDurationSec = 20,
                        cooldownDurationSec = 20,
                        preStartDelaySec = 5,
                        accentColorHex = "#FFC107",
                        isCustom = false,
                        displayOrder = 5
                    )
                )
                db.timerProfileDao().insertAll(defaultProfiles)
                db.userSettingsDao().insertOrUpdateSettings(UserSettings())
            }
        }
    }
}
