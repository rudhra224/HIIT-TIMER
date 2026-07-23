package com.example.service

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.WorkoutLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvExportManager(private val context: Context) {

    fun exportAndShareWorkoutLogs(logs: List<WorkoutLog>): File? {
        if (logs.isEmpty()) return null

        val csvHeader = "Log ID, Date & Time, Routine Name, Duration (Sec), Duration (Formatted), Calories Burned (kcal), Rounds Completed, Total Rounds\n"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val stringBuilder = StringBuilder(csvHeader)

        logs.forEach { log ->
            val dateStr = dateFormat.format(Date(log.completedAtTimestamp))
            val formattedDuration = "${log.durationSec / 60}m ${log.durationSec % 60}s"
            val safeName = log.routineName.replace(",", " ")
            stringBuilder.append("${log.id},\"$dateStr\",\"$safeName\",${log.durationSec},\"$formattedDuration\",${log.caloriesBurned},${log.roundsCompleted},${log.totalRounds}\n")
        }

        val fileName = "hiit_workout_history_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)

        return try {
            file.writeText(stringBuilder.toString())
            file
        } catch (e: Exception) {
            e.printStackTraceaccess
            null
        }
    }

    private val Exception.printStackTraceaccess: Unit
        get() = this.printStackTrace()

    fun shareCsvFile(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "HIIT Timer - Workout History Export")
            putExtra(Intent.EXTRA_TEXT, "Attached is my complete HIIT workout log export.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Export Workout History CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
