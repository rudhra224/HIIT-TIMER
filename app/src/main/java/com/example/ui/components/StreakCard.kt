package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.StreakSummary
import com.example.service.WeekDayStatus
import com.example.ui.theme.ElegantBorderColor
import com.example.ui.theme.ElegantLavender

@Composable
fun StreakCard(
    streakSummary: StreakSummary,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null
) {
    var isCalendarExpanded by remember { mutableStateOf(false) }

    val accentColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_card_container"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Main Top Row: Flame Icon + Streak Count + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Flame Icon Badge with Glow
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.35f),
                                        accentColor.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .border(1.5.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Workout Streak",
                            tint = if (streakSummary.currentStreakDays > 0) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (streakSummary.currentStreakDays == 1) "1 Day Active Streak!" else "${streakSummary.currentStreakDays} Days Active Streak!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.testTag("streak_count_text")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Best Streak: ${streakSummary.bestStreakDays} days • Total: ${streakSummary.totalActiveDaysCount} days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Today Status Badge Pill
                Surface(
                    color = if (streakSummary.hasWorkoutToday) {
                        accentColor.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (streakSummary.hasWorkoutToday) accentColor else Color.Transparent
                    ),
                    modifier = Modifier.testTag("today_status_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (streakSummary.hasWorkoutToday) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "TODAY DONE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = accentColor
                            )
                        } else {
                            Text(
                                text = "PENDING TODAY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Current Weekday Status Row (M T W T F S S)
            Text(
                text = "THIS WEEK'S CONSECUTIVE GOAL (${streakSummary.activeDaysThisWeek}/7 DAYS)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streak_week_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                streakSummary.currentWeekDaysStatus.forEach { dayStatus ->
                    WeekDayBubble(dayStatus = dayStatus, accentColor = accentColor)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Toggle for 28-Day Heatmap
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isCalendarExpanded = !isCalendarExpanded }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCalendarExpanded) "Hide 28-Day Streak Calendar" else "View 28-Day Streak Calendar",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
                Icon(
                    imageVector = if (isCalendarExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isCalendarExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    StreakHeatmapGrid(days = streakSummary.streakCalendarDays)
                }
            }
        }
    }
}

@Composable
fun StreakHeatmapGrid(days: List<com.example.ui.viewmodel.StreakDay>) {
    val accentColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "PAST 28 DAYS HEATMAP",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.chunked(7).forEach { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        week.forEach { day ->
                            val color = when (day.intensityLevel) {
                                1 -> accentColor.copy(alpha = 0.4f)
                                2 -> accentColor.copy(alpha = 0.7f)
                                3 -> accentColor
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.dayNumber.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (day.hasWorkout) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (day.hasWorkout) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekDayBubble(
    dayStatus: WeekDayStatus,
    accentColor: Color
) {
    val bubbleBg = when {
        dayStatus.hasWorkout -> accentColor
        dayStatus.isToday -> accentColor.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        dayStatus.hasWorkout -> Color.Black
        dayStatus.isToday -> accentColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = dayStatus.dayLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (dayStatus.isToday) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(bubbleBg)
                .border(
                    width = if (dayStatus.isToday) 2.dp else 0.dp,
                    color = if (dayStatus.isToday) accentColor else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (dayStatus.hasWorkout) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = dayStatus.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (dayStatus.isToday) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    ),
                    color = textColor
                )
            }
        }
    }
}
