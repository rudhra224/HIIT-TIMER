package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TimerProfile
import com.example.ui.theme.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditDialog(
    profile: TimerProfile?,
    onDismiss: () -> Unit,
    onSave: (TimerProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "Custom HIIT") }
    var workSec by remember { mutableFloatStateOf((profile?.workDurationSec ?: 30).toFloat()) }
    var restSec by remember { mutableFloatStateOf((profile?.restDurationSec ?: 15).toFloat()) }
    var setsCount by remember { mutableFloatStateOf((profile?.setsCount ?: 8).toFloat()) }
    var warmupSec by remember { mutableFloatStateOf((profile?.warmupDurationSec ?: 10).toFloat()) }
    var cooldownSec by remember { mutableFloatStateOf((profile?.cooldownDurationSec ?: 10).toFloat()) }
    var preStartDelaySec by remember { mutableStateOf(profile?.preStartDelaySec ?: 5) }
    var enableAudio by remember { mutableStateOf(profile?.enableAudio ?: true) }
    var enableHaptics by remember { mutableStateOf(profile?.enableHaptics ?: true) }
    var selectedColorHex by remember { mutableStateOf(profile?.accentColorHex ?: "#FF5722") }

    val availableColors = listOf("#FF5722", "#00E676", "#00E5FF", "#D500F9", "#FFC107", "#E91E63")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (profile == null) "Create HIIT Routine" else "Edit Routine",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_name_input")
                )

                // HIIT Preset Quick Selector Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Quick HIIT Presets", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                name = "Tabata Protocol"
                                workSec = 20f
                                restSec = 10f
                                setsCount = 8f
                                warmupSec = 10f
                                cooldownSec = 10f
                                selectedColorHex = "#FF5722"
                            },
                            label = { Text("Tabata 20/10", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                name = "EMOM Intense"
                                workSec = 50f
                                restSec = 10f
                                setsCount = 10f
                                warmupSec = 15f
                                cooldownSec = 15f
                                selectedColorHex = "#00E5FF"
                            },
                            label = { Text("EMOM 50/10", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                name = "30/30 Equal Rounds"
                                workSec = 30f
                                restSec = 30f
                                setsCount = 6f
                                warmupSec = 10f
                                cooldownSec = 10f
                                selectedColorHex = "#00E676"
                            },
                            label = { Text("30/30 Rounds", fontSize = 12.sp) }
                        )
                    }
                }

                // Accent Color Selector
                Text("Theme Accent Color", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableColors.forEach { hex ->
                        val color = parseHexColor(hex)
                        val isSelected = selectedColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                // Work Duration
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Work Duration", fontWeight = FontWeight.SemiBold)
                        Text("${workSec.toInt()} sec", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = workSec,
                        onValueChange = { workSec = it },
                        valueRange = 5f..180f,
                        steps = 34,
                        modifier = Modifier.testTag("work_duration_slider")
                    )
                }

                // Rest Duration
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rest Duration", fontWeight = FontWeight.SemiBold)
                        Text("${restSec.toInt()} sec", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = restSec,
                        onValueChange = { restSec = it },
                        valueRange = 0f..120f,
                        steps = 23,
                        modifier = Modifier.testTag("rest_duration_slider")
                    )
                }

                // Sets / Rounds Count
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rounds / Sets", fontWeight = FontWeight.SemiBold)
                        Text("${setsCount.toInt()} sets", fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = setsCount,
                        onValueChange = { setsCount = it },
                        valueRange = 1f..30f,
                        steps = 28,
                        modifier = Modifier.testTag("sets_count_slider")
                    )
                }

                // Warm-up & Cool-down
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Warm-up: ${warmupSec.toInt()}s", fontSize = 14.sp)
                        Slider(
                            value = warmupSec,
                            onValueChange = { warmupSec = it },
                            valueRange = 0f..60f,
                            steps = 11
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cool-down: ${cooldownSec.toInt()}s", fontSize = 14.sp)
                        Slider(
                            value = cooldownSec,
                            onValueChange = { cooldownSec = it },
                            valueRange = 0f..60f,
                            steps = 11
                        )
                    }
                }

                // Pre-Start Delay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("5-Sec Pre-Start Countdown", fontWeight = FontWeight.SemiBold)
                        Text("Get ready before 1st set", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = preStartDelaySec > 0,
                        onCheckedChange = { preStartDelaySec = if (it) 5 else 0 },
                        modifier = Modifier.testTag("pre_start_switch")
                    )
                }

                // Audio & Haptic Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Audio Beeps & Cues")
                    Switch(checked = enableAudio, onCheckedChange = { enableAudio = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Haptic Vibrations")
                    Switch(checked = enableHaptics, onCheckedChange = { enableHaptics = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newProfile = TimerProfile(
                        id = profile?.id ?: 0L,
                        name = name.ifBlank { "Custom HIIT" },
                        workDurationSec = workSec.toInt(),
                        restDurationSec = restSec.toInt(),
                        setsCount = setsCount.toInt(),
                        warmupDurationSec = warmupSec.toInt(),
                        cooldownDurationSec = cooldownSec.toInt(),
                        preStartDelaySec = preStartDelaySec,
                        enableAudio = enableAudio,
                        enableHaptics = enableHaptics,
                        accentColorHex = selectedColorHex,
                        isCustom = true,
                        displayOrder = profile?.displayOrder ?: 10
                    )
                    onSave(newProfile)
                },
                modifier = Modifier.testTag("save_routine_button")
            ) {
                Text("Save Routine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
