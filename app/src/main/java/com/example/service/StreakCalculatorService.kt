package com.example.service

import com.example.data.WorkoutLog
import com.example.ui.viewmodel.StreakDay
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

data class StreakSummary(
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val hasWorkoutToday: Boolean = false,
    val activeDaysThisWeek: Int = 0,
    val totalActiveDaysCount: Int = 0,
    val streakCalendarDays: List<StreakDay> = emptyList(),
    val currentWeekDaysStatus: List<WeekDayStatus> = emptyList()
)

data class WeekDayStatus(
    val dayLabel: String, // "M", "T", "W", "T", "F", "S", "S" or "Sun", "Mon"...
    val dayOfMonth: Int,
    val isToday: Boolean,
    val hasWorkout: Boolean,
    val dateTimestamp: Long
)

class StreakCalculatorService {

    fun calculateStreakSummary(
        logs: List<WorkoutLog>,
        heatmapDaysCount: Int = 28
    ): StreakSummary {
        if (logs.isEmpty()) {
            return StreakSummary(
                streakCalendarDays = generateEmptyHeatmap(heatmapDaysCount),
                currentWeekDaysStatus = generateCurrentWeekStatus(emptySet())
            )
        }

        val calendar = Calendar.getInstance()

        // Map date key "YYYY-DAY_OF_YEAR" to workout count and timestamps
        val workoutCountsByDayKey = mutableMapOf<String, Int>()
        val dayKeyToTimestamp = mutableMapOf<String, Long>()

        logs.forEach { log ->
            calendar.timeInMillis = log.completedAtTimestamp
            val key = getDayKey(calendar)
            workoutCountsByDayKey[key] = (workoutCountsByDayKey[key] ?: 0) + 1
            if (!dayKeyToTimestamp.containsKey(key)) {
                // normalize to start of day
                val startOfDay = calendar.clone() as Calendar
                startOfDay.set(Calendar.HOUR_OF_DAY, 0)
                startOfDay.set(Calendar.MINUTE, 0)
                startOfDay.set(Calendar.SECOND, 0)
                startOfDay.set(Calendar.MILLISECOND, 0)
                dayKeyToTimestamp[key] = startOfDay.timeInMillis
            }
        }

        val todayCal = Calendar.getInstance()
        val todayKey = getDayKey(todayCal)
        val hasWorkoutToday = workoutCountsByDayKey.containsKey(todayKey)

        // 1. Calculate Current Streak
        var currentStreak = 0
        val testCal = todayCal.clone() as Calendar

        // If today has a workout, current streak includes today.
        // If today has NO workout, we test yesterday. If yesterday had a workout, streak is alive!
        if (!hasWorkoutToday) {
            testCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val key = getDayKey(testCal)
            if (workoutCountsByDayKey.containsKey(key)) {
                currentStreak++
                testCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        // 2. Calculate Best Streak across all history
        val sortedUniqueDayTimestamps = dayKeyToTimestamp.values.sorted()
        var bestStreak = 0
        var tempStreak = 0
        var prevTimestamp: Long? = null

        for (timestamp in sortedUniqueDayTimestamps) {
            if (prevTimestamp == null) {
                tempStreak = 1
            } else {
                val diffMillis = timestamp - prevTimestamp
                val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
                if (diffDays == 1L) {
                    tempStreak++
                } else if (diffDays > 1L) {
                    tempStreak = 1
                }
            }
            if (tempStreak > bestStreak) {
                bestStreak = tempStreak
            }
            prevTimestamp = timestamp
        }

        if (currentStreak > bestStreak) {
            bestStreak = currentStreak
        }

        // 3. Generate Heatmap Calendar
        val heatmapDays = mutableListOf<StreakDay>()
        val loopCal = Calendar.getInstance()
        for (i in (heatmapDaysCount - 1) downTo 0) {
            val dayCal = loopCal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val key = getDayKey(dayCal)
            val count = workoutCountsByDayKey[key] ?: 0

            val intensity = when {
                count == 0 -> 0
                count == 1 -> 1
                count == 2 -> 2
                else -> 3
            }

            heatmapDays.add(
                StreakDay(
                    date = dayCal.time,
                    dayNumber = dayCal.get(Calendar.DAY_OF_MONTH),
                    hasWorkout = count > 0,
                    intensityLevel = intensity
                )
            )
        }

        // 4. Current Week Status
        val daysWithWorkoutsSet = workoutCountsByDayKey.keys.toSet()
        val weekStatus = generateCurrentWeekStatus(daysWithWorkoutsSet)
        val activeThisWeek = weekStatus.count { it.hasWorkout }

        return StreakSummary(
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak,
            hasWorkoutToday = hasWorkoutToday,
            activeDaysThisWeek = activeThisWeek,
            totalActiveDaysCount = workoutCountsByDayKey.size,
            streakCalendarDays = heatmapDays,
            currentWeekDaysStatus = weekStatus
        )
    }

    private fun getDayKey(calendar: Calendar): String {
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun generateCurrentWeekStatus(workoutDayKeys: Set<String>): List<WeekDayStatus> {
        val calendar = Calendar.getInstance()
        val todayKey = getDayKey(calendar)

        // Find start of current week (Monday or Sunday)
        val weekCal = calendar.clone() as Calendar
        weekCal.firstDayOfWeek = Calendar.MONDAY
        weekCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        val result = mutableListOf<WeekDayStatus>()

        for (i in 0..6) {
            val key = getDayKey(weekCal)
            val isToday = (key == todayKey)
            val hasWorkout = workoutDayKeys.contains(key)

            result.add(
                WeekDayStatus(
                    dayLabel = dayLabels[i],
                    dayOfMonth = weekCal.get(Calendar.DAY_OF_MONTH),
                    isToday = isToday,
                    hasWorkout = hasWorkout,
                    dateTimestamp = weekCal.timeInMillis
                )
            )
            weekCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return result
    }

    private fun generateEmptyHeatmap(count: Int): List<StreakDay> {
        val result = mutableListOf<StreakDay>()
        val cal = Calendar.getInstance()
        for (i in (count - 1) downTo 0) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            result.add(
                StreakDay(
                    date = dayCal.time,
                    dayNumber = dayCal.get(Calendar.DAY_OF_MONTH),
                    hasWorkout = false,
                    intensityLevel = 0
                )
            )
        }
        return result
    }
}
