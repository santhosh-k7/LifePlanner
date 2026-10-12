package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayCircle
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.RoutineStep
import com.example.data.model.RoutineWithSteps
import com.example.util.DateTimeUtils

@Composable
fun RoutineCard(
    routineWithSteps: RoutineWithSteps,
    onRunRoutine: () -> Unit,
    onToggleStep: (Long, Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routine = routineWithSteps.routine
    val steps = routineWithSteps.steps
    val todayStr = DateTimeUtils.todayStr()
    val completedStepsCount = steps.count { it.completedDate == todayStr }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("routine_card_${routine.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = routine.iconEmoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${routine.timeOfDay} · ${DateTimeUtils.formatTime12H(routine.startTime)} · $completedStepsCount/${steps.size} done",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onRunRoutine,
                    modifier = Modifier.testTag("run_routine_btn_${routine.id}"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Linear Progress of completion
            val progress = if (steps.isNotEmpty()) completedStepsCount.toFloat() / steps.size else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Expand / Collapse steps checklist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (expanded) "Hide steps checklist" else "View ${steps.size} steps checklist",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    steps.forEach { step ->
                        val isDone = step.completedDate == todayStr
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleStep(step.id, !isDone) }
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isDone,
                                onCheckedChange = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (step.durationMin > 0) {
                                Text(
                                    text = "${step.durationMin}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Routine", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineRunnerDialog(
    routineWithSteps: RoutineWithSteps,
    currentStepIndex: Int,
    secondsRemaining: Int,
    isTimerRunning: Boolean,
    onPauseResumeTimer: () -> Unit,
    onNextStep: () -> Unit,
    onClose: () -> Unit
) {
    val routine = routineWithSteps.routine
    val steps = routineWithSteps.steps
    val currentStep = steps.getOrNull(currentStepIndex)
    val nextStep = steps.getOrNull(currentStepIndex + 1)

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("routine_runner_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = routine.iconEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = routine.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close runner")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Indicator
                val stepProgress = (currentStepIndex + 1).toFloat() / steps.size
                LinearProgressIndicator(
                    progress = { stepProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Text(
                    text = "Step ${currentStepIndex + 1} of ${steps.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Current Step Hero
                if (currentStep != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentStep.title,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Large Step Countdown Timer
                            Text(
                                text = DateTimeUtils.formatSecondsToMmSs(secondsRemaining),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Play / Pause Step Timer
                            IconButton(
                                onClick = onPauseResumeTimer,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle timer",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                if (nextStep != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Next: ${nextStep.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Button: Done / Next Step
                Button(
                    onClick = onNextStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("routine_runner_next_btn"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentStepIndex == steps.size - 1) "Complete Routine" else "Done · Next Step",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
