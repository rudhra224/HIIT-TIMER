package com.example.data

import kotlinx.coroutines.flow.Flow

class HiitRepository(
    private val timerProfileDao: TimerProfileDao,
    private val workoutLogDao: WorkoutLogDao,
    private val userSettingsDao: UserSettingsDao
) {
    val allProfiles: Flow<List<TimerProfile>> = timerProfileDao.getAllProfiles()
    val allWorkoutLogs: Flow<List<WorkoutLog>> = workoutLogDao.getAllLogs()
    val userSettingsFlow: Flow<UserSettings?> = userSettingsDao.getUserSettingsFlow()

    val totalWorkoutSeconds: Flow<Int?> = workoutLogDao.getTotalWorkoutSecondsFlow()
    val totalCaloriesBurned: Flow<Int?> = workoutLogDao.getTotalCaloriesBurnedFlow()
    val totalWorkoutsCompleted: Flow<Int> = workoutLogDao.getTotalWorkoutsCompletedFlow()

    suspend fun getProfileById(id: Long): TimerProfile? = timerProfileDao.getProfileById(id)

    suspend fun insertProfile(profile: TimerProfile): Long = timerProfileDao.insertProfile(profile)

    suspend fun updateProfile(profile: TimerProfile) = timerProfileDao.updateProfile(profile)

    suspend fun deleteProfile(profile: TimerProfile) = timerProfileDao.deleteProfile(profile)

    suspend fun insertWorkoutLog(log: WorkoutLog): Long = workoutLogDao.insertLog(log)

    suspend fun getAllWorkoutLogsList(): List<WorkoutLog> = workoutLogDao.getAllLogsList()

    suspend fun saveUserSettings(settings: UserSettings) = userSettingsDao.insertOrUpdateSettings(settings)

    suspend fun getUserSettings(): UserSettings = userSettingsDao.getUserSettings() ?: UserSettings()
}
