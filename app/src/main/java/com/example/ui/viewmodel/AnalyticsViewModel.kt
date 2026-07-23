package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HiitRepository
import com.example.data.WorkoutLog
import com.example.service.CsvExportManager
import com.example.service.StreakCalculatorService
import com.example.service.StreakSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

enum class TimeFilter {
    WEEKLY, MONTHLY, YEARLY
}

data class AnalyticsUiState(
    val totalMinutes: Int = 0,
    val totalCalories: Int = 0,
    val totalSessions: Int = 0,
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val selectedFilter: TimeFilter = TimeFilter.WEEKLY,
    val chartEntries: List<ChartDataPoint> = emptyList(),
    val streakHeatmap: List<StreakDay> = emptyList(),
    val streakSummary: StreakSummary = StreakSummary(),
    val workoutLogs: List<WorkoutLog> = emptyList()
)

data class ChartDataPoint(
    val label: String,
    val minutes: Float,
    val calories: Float
)

data class StreakDay(
    val date: Date,
    val dayNumber: Int,
    val hasWorkout: Boolean,
    val intensityLevel: Int // 0: None, 1: Low, 2: Medium, 3: High
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HiitRepository
    private val csvExportManager = CsvExportManager(application)
    private val streakCalculatorService = StreakCalculatorService()

    private val _selectedFilter = MutableStateFlow(TimeFilter.WEEKLY)

    val uiState: StateFlow<AnalyticsUiState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HiitRepository(db.timerProfileDao(), db.workoutLogDao(), db.userSettingsDao())

        uiState = combine(
            repository.allWorkoutLogs,
            _selectedFilter
        ) { logs, filter ->
            computeAnalyticsState(logs, filter)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalyticsUiState()
        )
    }

    fun setFilter(filter: TimeFilter) {
        _selectedFilter.value = filter
    }

    fun exportCsv() {
        viewModelScope.launch {
            val logs = repository.getAllWorkoutLogsList()
            val file = csvExportManager.exportAndShareWorkoutLogs(logs)
            file?.let {
                csvExportManager.shareCsvFile(it)
            }
        }
    }

    private fun computeAnalyticsState(logs: List<WorkoutLog>, filter: TimeFilter): AnalyticsUiState {
        val totalSecs = logs.sumOf { it.durationSec }
        val totalMins = totalSecs / 60
        val totalCals = logs.sumOf { it.caloriesBurned }
        val totalSessions = logs.size

        val streakSummary = streakCalculatorService.calculateStreakSummary(logs)
        val chartPoints = generateChartPoints(logs, filter)

        return AnalyticsUiState(
            totalMinutes = totalMins,
            totalCalories = totalCals,
            totalSessions = totalSessions,
            currentStreakDays = streakSummary.currentStreakDays,
            bestStreakDays = streakSummary.bestStreakDays,
            selectedFilter = filter,
            chartEntries = chartPoints,
            streakHeatmap = streakSummary.streakCalendarDays,
            streakSummary = streakSummary,
            workoutLogs = logs
        )
    }

    private fun generateChartPoints(logs: List<WorkoutLog>, filter: TimeFilter): List<ChartDataPoint> {
        val points = mutableListOf<ChartDataPoint>()
        val calendar = Calendar.getInstance()

        when (filter) {
            TimeFilter.WEEKLY -> {
                val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                val dayMins = FloatArray(7)
                val dayCals = FloatArray(7)

                val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }

                logs.filter { it.completedAtTimestamp >= weekAgo.timeInMillis }.forEach { log ->
                    calendar.timeInMillis = log.completedAtTimestamp
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun
                    dayMins[dayOfWeek] += log.durationSec / 60f
                    dayCals[dayOfWeek] += log.caloriesBurned.toFloat()
                }

                for (i in 0..6) {
                    points.add(ChartDataPoint(dayNames[i], dayMins[i], dayCals[i]))
                }
            }

            TimeFilter.MONTHLY -> {
                val monthAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -28) }
                val weekMins = FloatArray(4)
                val weekCals = FloatArray(4)

                logs.filter { it.completedAtTimestamp >= monthAgo.timeInMillis }.forEach { log ->
                    val diffDays = ((log.completedAtTimestamp - monthAgo.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                    val weekIdx = (diffDays / 7).coerceIn(0, 3)
                    weekMins[weekIdx] += log.durationSec / 60f
                    weekCals[weekIdx] += log.caloriesBurned.toFloat()
                }

                for (w in 0..3) {
                    points.add(ChartDataPoint("W${w + 1}", weekMins[w], weekCals[w]))
                }
            }

            TimeFilter.YEARLY -> {
                val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val monthMins = FloatArray(12)
                val monthCals = FloatArray(12)

                val yearAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }

                logs.filter { it.completedAtTimestamp >= yearAgo.timeInMillis }.forEach { log ->
                    calendar.timeInMillis = log.completedAtTimestamp
                    val m = calendar.get(Calendar.MONTH)
                    monthMins[m] += log.durationSec / 60f
                    monthCals[m] += log.caloriesBurned.toFloat()
                }

                for (m in 0..11) {
                    points.add(ChartDataPoint(months[m], monthMins[m], monthCals[m]))
                }
            }
        }

        return points
    }
}
