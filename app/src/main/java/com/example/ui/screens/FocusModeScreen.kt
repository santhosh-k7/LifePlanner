package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.ui.viewmodel.PomodoroPhase
import com.example.util.DateTimeUtils

@Composable
fun FocusModeScreen(
    viewModel: LifePlannerViewModel,
    modifier: Modifier = Modifier
) {
    val focusTask by viewModel.focusTask.collectAsState()
    val phase by viewModel.focusPhase.collectAsState()
    val round by viewModel.focusRound.collectAsState()
    val remainingSec by viewModel.timerRemainingSeconds.collectAsState()
    val totalSec by viewModel.timerTotalSeconds.collectAsState()
    val isRunning by viewModel.isTimerActive.collectAsState()

    val progress = if (totalSec > 0) {
        remainingSec.toFloat() / totalSec.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timer_progress")

    val phaseColor = when (phase) {
        PomodoroPhase.FOCUS -> MaterialTheme.colorScheme.primary
        PomodoroPhase.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
        PomodoroPhase.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
        PomodoroPhase.STOPWATCH -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_mode_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.closeFocusScreen() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Focus Mode",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = phaseColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "${phase.title} · Round $round of 4",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = phaseColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                IconButton(onClick = { viewModel.resetFocusTimer() }) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Reset Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Task Spotlight Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = focusTask?.title ?: "Deep Focus Session",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                if (focusTask != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Category.getColor(focusTask!!.categoryId).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = focusTask!!.categoryId.replaceFirstChar { it.uppercase() },
                            color = Category.getColor(focusTask!!.categoryId),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Circular Visual Timer
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .testTag("circular_focus_timer"),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Background ring track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Foreground active ring
                    drawArc(
                        color = phaseColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Time Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = DateTimeUtils.formatSecondsToMmSs(remainingSec),
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-1).sp
                    )

                    Text(
                        text = if (isRunning) "Stay with it" else "Paused",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = phaseColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Sound & haptics on complete",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = phaseColor.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause FAB
                FilledIconButton(
                    onClick = { viewModel.pauseResumeFocusTimer() },
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("play_pause_focus_btn"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = phaseColor
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // "I'm Done" / Complete Task Button
            if (focusTask != null) {
                OutlinedButton(
                    onClick = { viewModel.completeTaskFromFocus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("finish_focus_task_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I'm Done · Complete Task",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.closeFocusScreen() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Finish Focus Session", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
