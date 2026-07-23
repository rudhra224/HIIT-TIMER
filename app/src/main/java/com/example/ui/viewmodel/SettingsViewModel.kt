package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HiitRepository
import com.example.data.UserSettings
import com.example.service.NotificationScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HiitRepository
    private val notificationScheduler = NotificationScheduler(application)

    val userSettingsState: StateFlow<UserSettings?>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HiitRepository(db.timerProfileDao(), db.workoutLogDao(), db.userSettingsDao())
        userSettingsState = repository.userSettingsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )
    }

    fun updateSettings(newSettings: UserSettings) {
        viewModelScope.launch {
            repository.saveUserSettings(newSettings)
            if (newSettings.enableDailyReminder) {
                notificationScheduler.scheduleDailyReminder(newSettings.notificationTimeMinuteOfDay)
            } else {
                notificationScheduler.cancelReminder()
            }
        }
    }
}
