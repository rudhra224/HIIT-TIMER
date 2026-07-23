package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HiitRepository
import com.example.data.TimerProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutinesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HiitRepository

    val profilesState: StateFlow<List<TimerProfile>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HiitRepository(db.timerProfileDao(), db.workoutLogDao(), db.userSettingsDao())
        profilesState = repository.allProfiles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addOrUpdateProfile(profile: TimerProfile) {
        viewModelScope.launch {
            if (profile.id == 0L) {
                repository.insertProfile(profile)
            } else {
                repository.updateProfile(profile)
            }
        }
    }

    fun deleteProfile(profile: TimerProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }
}
