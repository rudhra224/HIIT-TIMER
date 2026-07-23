package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HiitRepository
import com.example.data.TimerProfile
import com.example.data.WorkoutLog
import com.example.service.AudioHapticManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TimerPhase(val displayName: String) {
    PRE_START("Get Ready"),
    WARM_UP("Warm-Up"),
    WORK("WORK!"),
    REST("Rest"),
    COOL_DOWN("Cool-Down"),
    FINISHED("Finished!"),
    PAUSED("Paused")
}

data class TimerUiState(
    val profile: TimerProfile? = null,
    val currentPhase: TimerPhase = TimerPhase.PRE_START,
    val previousPhaseBeforePause: TimerPhase = TimerPhase.PRE_START,
    val phaseRemainingSec: Int = 0,
    val phaseTotalSec: Int = 0,
    val currentSet: Int = 1,
    val totalSets: Int = 1,
    val totalElapsedSec: Int = 0,
    val isRunning: Boolean = false,
    val isMuted: Boolean = false,
    val isFinished: Boolean = false,
    val lastCompletedLog: WorkoutLog? = null
)

class ActiveTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HiitRepository
    private val audioHapticManager = AudioHapticManager(application)

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HiitRepository(db.timerProfileDao(), db.workoutLogDao(), db.userSettingsDao())
    }

    fun startTimerWithProfile(profile: TimerProfile) {
        timerJob?.cancel()
        val initialPhase = if (profile.preStartDelaySec > 0) TimerPhase.PRE_START else {
            if (profile.warmupDurationSec > 0) TimerPhase.WARM_UP else TimerPhase.WORK
        }
        val initialDuration = when (initialPhase) {
            TimerPhase.PRE_START -> profile.preStartDelaySec
            TimerPhase.WARM_UP -> profile.warmupDurationSec
            TimerPhase.WORK -> profile.workDurationSec
            else -> 0
        }

        _uiState.update {
            TimerUiState(
                profile = profile,
                currentPhase = initialPhase,
                phaseRemainingSec = initialDuration,
                phaseTotalSec = initialDuration,
                currentSet = 1,
                totalSets = profile.setsCount,
                totalElapsedSec = 0,
                isRunning = true,
                isMuted = !profile.enableAudio,
                isFinished = false,
                lastCompletedLog = null
            )
        }

        runTimerLoop()
    }

    fun togglePlayPause() {
        val currentState = _uiState.value
        if (currentState.isFinished) return

        if (currentState.isRunning) {
            // Pause
            timerJob?.cancel()
            _uiState.update {
                it.copy(
                    isRunning = false,
                    previousPhaseBeforePause = it.currentPhase,
                    currentPhase = TimerPhase.PAUSED
                )
            }
        } else {
            // Resume
            val resumedPhase = if (currentState.currentPhase == TimerPhase.PAUSED) currentState.previousPhaseBeforePause else currentState.currentPhase
            _uiState.update {
                it.copy(
                    isRunning = true,
                    currentPhase = resumedPhase
                )
            }
            runTimerLoop()
        }
    }

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun resetTimer() {
        timerJob?.cancel()
        val profile = _uiState.value.profile ?: return
        startTimerWithProfile(profile)
    }

    fun skipToNextPhase() {
        timerJob?.cancel()
        advanceToNextPhase()
        if (_uiState.value.isRunning && !_uiState.value.isFinished) {
            runTimerLoop()
        }
    }

    fun skipToPreviousPhase() {
        timerJob?.cancel()
        val currentState = _uiState.value
        val profile = currentState.profile ?: return

        when (currentState.currentPhase) {
            TimerPhase.REST -> {
                _uiState.update {
                    it.copy(
                        currentPhase = TimerPhase.WORK,
                        phaseRemainingSec = profile.workDurationSec,
                        phaseTotalSec = profile.workDurationSec
                    )
                }
            }
            TimerPhase.WORK -> {
                if (currentState.currentSet > 1) {
                    _uiState.update {
                        it.copy(
                            currentSet = it.currentSet - 1,
                            currentPhase = TimerPhase.REST,
                            phaseRemainingSec = profile.restDurationSec,
                            phaseTotalSec = profile.restDurationSec
                        )
                    }
                } else if (profile.warmupDurationSec > 0) {
                    _uiState.update {
                        it.copy(
                            currentPhase = TimerPhase.WARM_UP,
                            phaseRemainingSec = profile.warmupDurationSec,
                            phaseTotalSec = profile.warmupDurationSec
                        )
                    }
                }
            }
            else -> {}
        }

        if (currentState.isRunning) {
            runTimerLoop()
        }
    }

    private fun runTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning && !_uiState.value.isFinished) {
                delay(1000L)
                val currentState = _uiState.value
                val audioEnabled = !currentState.isMuted && (currentState.profile?.enableAudio ?: true)
                val hapticsEnabled = currentState.profile?.enableHaptics ?: true

                if (currentState.phaseRemainingSec > 1) {
                    // Decrement current phase
                    val nextRemaining = currentState.phaseRemainingSec - 1
                    _uiState.update {
                        it.copy(
                            phaseRemainingSec = nextRemaining,
                            totalElapsedSec = it.totalElapsedSec + 1
                        )
                    }

                    // 3-2-1 countdown beep
                    if (nextRemaining <= 3 && nextRemaining > 0) {
                        audioHapticManager.playCountdownBeep(audioEnabled, hapticsEnabled)
                    }
                } else {
                    // Phase completed -> transition
                    _uiState.update { it.copy(totalElapsedSec = it.totalElapsedSec + 1) }
                    advanceToNextPhase()
                }
            }
        }
    }

    private fun advanceToNextPhase() {
        val state = _uiState.value
        val profile = state.profile ?: return
        val audioEnabled = !state.isMuted && profile.enableAudio
        val hapticsEnabled = profile.enableHaptics

        when (state.currentPhase) {
            TimerPhase.PRE_START -> {
                if (profile.warmupDurationSec > 0) {
                    _uiState.update {
                        it.copy(
                            currentPhase = TimerPhase.WARM_UP,
                            phaseRemainingSec = profile.warmupDurationSec,
                            phaseTotalSec = profile.warmupDurationSec
                        )
                    }
                    audioHapticManager.playRestStartSound(audioEnabled, hapticsEnabled)
                } else {
                    _uiState.update {
                        it.copy(
                            currentPhase = TimerPhase.WORK,
                            phaseRemainingSec = profile.workDurationSec,
                            phaseTotalSec = profile.workDurationSec
                        )
                    }
                    audioHapticManager.playWorkStartSound(audioEnabled, hapticsEnabled)
                }
            }

            TimerPhase.WARM_UP -> {
                _uiState.update {
                    it.copy(
                        currentPhase = TimerPhase.WORK,
                        phaseRemainingSec = profile.workDurationSec,
                        phaseTotalSec = profile.workDurationSec
                    )
                }
                audioHapticManager.playWorkStartSound(audioEnabled, hapticsEnabled)
            }

            TimerPhase.WORK -> {
                if (state.currentSet < profile.setsCount) {
                    if (profile.restDurationSec > 0) {
                        _uiState.update {
                            it.copy(
                                currentPhase = TimerPhase.REST,
                                phaseRemainingSec = profile.restDurationSec,
                                phaseTotalSec = profile.restDurationSec
                            )
                        }
                        audioHapticManager.playRestStartSound(audioEnabled, hapticsEnabled)
                    } else {
                        // Immediate next set
                        _uiState.update {
                            it.copy(
                                currentSet = it.currentSet + 1,
                                currentPhase = TimerPhase.WORK,
                                phaseRemainingSec = profile.workDurationSec,
                                phaseTotalSec = profile.workDurationSec
                            )
                        }
                        audioHapticManager.playWorkStartSound(audioEnabled, hapticsEnabled)
                    }
                } else {
                    // All sets done
                    if (profile.cooldownDurationSec > 0) {
                        _uiState.update {
                            it.copy(
                                currentPhase = TimerPhase.COOL_DOWN,
                                phaseRemainingSec = profile.cooldownDurationSec,
                                phaseTotalSec = profile.cooldownDurationSec
                            )
                        }
                        audioHapticManager.playRestStartSound(audioEnabled, hapticsEnabled)
                    } else {
                        finishWorkout()
                    }
                }
            }

            TimerPhase.REST -> {
                _uiState.update {
                    it.copy(
                        currentSet = it.currentSet + 1,
                        currentPhase = TimerPhase.WORK,
                        phaseRemainingSec = profile.workDurationSec,
                        phaseTotalSec = profile.workDurationSec
                    )
                }
                audioHapticManager.playWorkStartSound(audioEnabled, hapticsEnabled)
            }

            TimerPhase.COOL_DOWN -> {
                finishWorkout()
            }

            else -> {}
        }
    }

    private fun finishWorkout() {
        timerJob?.cancel()
        val state = _uiState.value
        val profile = state.profile ?: return
        val audioEnabled = !state.isMuted && profile.enableAudio
        val hapticsEnabled = profile.enableHaptics

        // Calculate calories: approx 10 kcal per minute of work phase
        val totalWorkTimeMins = (profile.setsCount * profile.workDurationSec) / 60.0
        val estimatedCalories = (totalWorkTimeMins * 11.5).toInt().coerceAtLeast(15)

        val log = WorkoutLog(
            profileId = profile.id,
            routineName = profile.name,
            durationSec = state.totalElapsedSec,
            caloriesBurned = estimatedCalories,
            roundsCompleted = profile.setsCount,
            totalRounds = profile.setsCount,
            completedAtTimestamp = System.currentTimeMillis()
        )

        audioHapticManager.playWorkoutFinishSound(audioEnabled, hapticsEnabled)

        viewModelScope.launch {
            repository.insertWorkoutLog(log)
            _uiState.update {
                it.copy(
                    currentPhase = TimerPhase.FINISHED,
                    isRunning = false,
                    isFinished = true,
                    phaseRemainingSec = 0,
                    lastCompletedLog = log
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        audioHapticManager.release()
    }
}
