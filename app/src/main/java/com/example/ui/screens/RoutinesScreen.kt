package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Routine
import com.example.data.model.RoutineStep
import com.example.ui.components.RoutineCard
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.viewmodel.LifePlannerViewModel

@Composable
fun RoutinesScreen(
    viewModel: LifePlannerViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = when (themeMode) {
        0 -> androidx.compose.foundation.isSystemInDarkTheme()
        1 -> false
        else -> true
    }

    val routines by viewModel.allRoutines.collectAsState()
    var showAddRoutineDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("routines_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daily Routines",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Guided step-by-step rituals for focus and flow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Button(
                onClick = { showAddRoutineDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("btn_new_routine")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Routine", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("routines_list_view"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (routines.isEmpty()) {
                item {
                    LiquidGlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Checklist,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Routines Created",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create a Morning Reset or Evening Wind-down ritual with timed steps.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(routines, key = { it.routine.id }) { routineItem ->
                    RoutineCard(
                        routineWithSteps = routineItem,
                        onRunRoutine = { viewModel.startRoutineRunner(routineItem) },
                        onToggleStep = { stepId, done -> viewModel.markRoutineStepDone(stepId, done) },
                        onDelete = { viewModel.deleteRoutine(routineItem.routine) }
                    )
                }
            }

            // Info Card
            item {
                LiquidGlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tip: Tap 'Start Routine' on any routine card to launch a guided, distraction-free step timer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAddRoutineDialog) {
        AddRoutineDialog(
            viewModel = viewModel,
            onDismiss = { showAddRoutineDialog = false }
        )
    }
}

@Composable
private fun AddRoutineDialog(
    viewModel: LifePlannerViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var iconEmoji by remember { mutableStateOf("⚡") }
    var timeOfDay by remember { mutableStateOf("Morning") }
    var startTime by remember { mutableStateOf("08:00") }

    val emojiOptions = listOf("⚡", "☀️", "🌙", "🧘", "🚀", "💻", "📚", "✨")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Create New Routine",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name") },
                    placeholder = { Text("e.g. Deep Work Warm-Up") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Emoji Picker
                Text("Icon Symbol", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojiOptions.forEach { emoji ->
                        FilledIconToggleButton(
                            checked = iconEmoji == emoji,
                            onCheckedChange = { iconEmoji = emoji },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text(text = emoji, fontSize = 16.sp)
                        }
                    }
                }

                // Time of Day
                Text("Time of Day", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Morning", "Afternoon", "Evening", "Night").forEach { tod ->
                        FilterChip(
                            selected = timeOfDay == tod,
                            onClick = { timeOfDay = tod },
                            label = { Text(tod, fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Scheduled Start Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addRoutine(
                                    name = name.trim(),
                                    icon = iconEmoji,
                                    timeOfDay = timeOfDay,
                                    startTime = startTime.ifBlank { "08:00" },
                                    steps = listOf(
                                        "Step 1: Get ready & focus" to 3,
                                        "Step 2: Execute core task" to 25
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Text("Save Routine")
                    }
                }
            }
        }
    }
}
