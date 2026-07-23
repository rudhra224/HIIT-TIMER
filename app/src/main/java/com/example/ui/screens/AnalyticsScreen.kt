package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.CsvExportManager
import com.example.ui.components.StreakCard
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.NeonCyan
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.ChartDataPoint
import com.example.ui.viewmodel.StreakDay
import com.example.ui.viewmodel.TimeFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Workout Analytics",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Track your streak, calories, & history",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.exportCsv() },
                        modifier = Modifier.testTag("export_csv_top_button")
                    ) {
                        Icon(Icons.Default.IosShare, contentDescription = "Export CSV")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // KPI Summary Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Workout Minutes",
                        value = "${uiState.totalMinutes}m",
                        icon = Icons.Default.Timer,
                        accentColor = FlameOrange,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Est. Calories",
                        value = "${uiState.totalCalories}",
                        unit = "kcal",
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = ElectricLime,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Streak Summary Card with Consecutive Active Days and Calendar Heatmap
            item {
                StreakCard(streakSummary = uiState.streakSummary)
            }

            // Filter Selector & Analytics Bar Chart
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Activity Progress",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TimeFilter.values().forEach { filter ->
                                FilterChip(
                                    selected = uiState.selectedFilter == filter,
                                    onClick = { viewModel.setFilter(filter) },
                                    label = { Text(filter.name, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            AnalyticsBarChart(dataPoints = uiState.chartEntries)
                        }
                    }
                }
            }

            // One-Click CSV Export Action Button
            item {
                Button(
                    onClick = { viewModel.exportCsv() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("one_click_csv_export_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Workout History to CSV", fontWeight = FontWeight.Bold)
                }
            }

            // Recent Workout History Header
            item {
                Text(
                    text = "Recent Workout History",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.workoutLogs.isEmpty()) {
                item {
                    Text(
                        text = "No workouts recorded yet. Complete a HIIT routine to start tracking history!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(uiState.workoutLogs) { log ->
                    WorkoutLogItem(log = log)
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    unit: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AnalyticsBarChart(dataPoints: List<ChartDataPoint>) {
    if (dataPoints.isEmpty()) return

    val maxVal = (dataPoints.maxOfOrNull { it.minutes } ?: 10f).coerceAtLeast(10f)

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val barColor = FlameOrange
            val trackColor = MaterialTheme.colorScheme.surfaceVariant

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val barCount = dataPoints.size
                val spaceBetween = width / barCount
                val barWidth = (spaceBetween * 0.45f).coerceAtMost(32.dp.toPx())

                dataPoints.forEachIndexed { index, point ->
                    val x = index * spaceBetween + (spaceBetween - barWidth) / 2
                    val barHeight = (point.minutes / maxVal) * (height - 30.dp.toPx())

                    // Draw background track
                    drawRoundRect(
                        color = trackColor,
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, height - 20.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    // Draw active bar
                    if (barHeight > 0) {
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, (height - 20.dp.toPx()) - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dataPoints.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WorkoutLogItem(log: com.example.data.WorkoutLog) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = log.routineName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = dateFormat.format(Date(log.completedAtTimestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${log.durationSec / 60}m ${log.durationSec % 60}s",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "~${log.caloriesBurned} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = FlameOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
