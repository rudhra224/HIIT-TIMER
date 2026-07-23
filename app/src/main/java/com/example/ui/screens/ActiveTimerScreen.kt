package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TimerProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveTimerViewModel
import com.example.ui.viewmodel.TimerPhase
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTimerScreen(
    viewModel: ActiveTimerViewModel,
    selectedProfile: TimerProfile?,
    onNavigateBackToRoutines: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Initialize timer if selectedProfile changed and not currently running
    LaunchedEffect(selectedProfile) {
        if (selectedProfile != null && (uiState.profile?.id != selectedProfile.id || uiState.isFinished)) {
            viewModel.startTimerWithProfile(selectedProfile)
        }
    }

    val currentProfile = uiState.profile ?: selectedProfile

    if (currentProfile == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "No Active Workout Selected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onNavigateBackToRoutines,
                    modifier = Modifier.testTag("go_to_routines_button")
                ) {
                    Text("Select a HIIT Routine")
                }
            }
        }
        return
    }

    // Determine color based on phase
    val phaseColor by animateColorAsState(
        targetValue = when (uiState.currentPhase) {
            TimerPhase.WORK -> PhaseWorkColor
            TimerPhase.REST -> PhaseRestColor
            TimerPhase.WARM_UP -> PhaseWarmupColor
            TimerPhase.COOL_DOWN -> PhaseCooldownColor
            TimerPhase.PRE_START -> PhasePreStartColor
            TimerPhase.PAUSED -> MaterialTheme.colorScheme.onSurfaceVariant
            TimerPhase.FINISHED -> ElectricLime
        },
        animationSpec = tween(durationMillis = 400),
        label = "phaseColor"
    )

    // Calculate ring progress
    val progressFraction = if (uiState.phaseTotalSec > 0) {
        (uiState.phaseRemainingSec.toFloat() / uiState.phaseTotalSec.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentProfile.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBackToRoutines,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier.testTag("mute_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Toggle Sound",
                            tint = if (uiState.isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier.testTag("reset_timer_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Timer")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Stats Header: Total Elapsed & Remaining
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ELAPSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTimeMmSs(uiState.totalElapsedSec),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TOTAL ESTIMATED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentProfile.formattedTotalTime(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Central Circular Timer Display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(16.dp)
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceVariant

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 20.dp.toPx()
                    // Background track
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = strokeWidth)
                    )
                    // Animated Arc
                    drawArc(
                        color = phaseColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Phase Badge Pill
                    Surface(
                        color = phaseColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, phaseColor)
                    ) {
                        Text(
                            text = uiState.currentPhase.displayName.uppercase(),
                            color = phaseColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remaining time digit
                    Text(
                        text = formatTimeMmSs(uiState.phaseRemainingSec),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black
                        ),
                        modifier = Modifier.testTag("remaining_time_text")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Round Counter
                    Text(
                        text = "ROUND ${uiState.currentSet} / ${uiState.totalSets}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Next Up Preview Banner
            val nextUpText = remember(uiState.currentPhase, uiState.currentSet) {
                when (uiState.currentPhase) {
                    TimerPhase.PRE_START -> "Next: ${if (currentProfile.warmupDurationSec > 0) "Warm-Up" else "WORK!"}"
                    TimerPhase.WARM_UP -> "Next: WORK (${currentProfile.workDurationSec}s)"
                    TimerPhase.WORK -> if (uiState.currentSet < currentProfile.setsCount) "Next: REST (${currentProfile.restDurationSec}s)" else "Next: Cool-Down"
                    TimerPhase.REST -> "Next: WORK Round ${uiState.currentSet + 1}"
                    TimerPhase.COOL_DOWN -> "Next: Finish!"
                    else -> ""
                }
            }

            if (nextUpText.isNotEmpty()) {
                Text(
                    text = nextUpText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bottom Timer Controls: Previous | Play/Pause | Next
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous
                IconButton(
                    onClick = { viewModel.skipToPreviousPhase() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("previous_phase_button")
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous Interval",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Main Play/Pause Button
                FloatingActionButton(
                    onClick = { viewModel.togglePlayPause() },
                    shape = CircleShape,
                    containerColor = phaseColor,
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(80.dp)
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isRunning) "Pause" else "Play",
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = { viewModel.skipToNextPhase() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("next_phase_button")
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next Interval",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    // Victory Celebration Dialog
    if (uiState.isFinished && uiState.lastCompletedLog != null) {
        val log = uiState.lastCompletedLog!!
        AlertDialog(
            onDismissRequest = { },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥 WORKOUT COMPLETE!", fontWeight = FontWeight.Black, fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("You crushed your ${currentProfile.name} session!", style = MaterialTheme.typography.bodyMedium)
                }
            },
            text = {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Total Duration:")
                            Text("${log.durationSec / 60}m ${log.durationSec % 60}s", fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Calories Burned:")
                            Text("~${log.caloriesBurned} kcal", fontWeight = FontWeight.Bold, color = PhaseWorkColor)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Rounds Completed:")
                            Text("${log.roundsCompleted} / ${log.totalRounds}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🔥 Completed my HIIT Workout: ${currentProfile.name}! (${log.durationSec / 60}m, ~${log.caloriesBurned} kcal burned) #HIIT #FitnessGoal"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Workout Milestone"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_milestone_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Achievement Card", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNavigateBackToRoutines,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Return to Routines")
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

fun formatTimeMmSs(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.US, "%02d:%02d", m, s)
}
