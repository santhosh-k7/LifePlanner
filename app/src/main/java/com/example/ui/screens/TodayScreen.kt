package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.RoutineWithSteps
import com.example.data.model.Task
import com.example.data.model.TaskWithSubtasks
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: LifePlannerViewModel,
    modifier: Modifier = Modifier
) {
    val todayTasks by viewModel.todayTasks.collectAsState()
    val overdueTasks by viewModel.overdueTasks.collectAsState()
    val routines by viewModel.allRoutines.collectAsState()
    val isSuggestionDismissed by viewModel.isSmartSuggestionDismissed.collectAsState()

    // Filter today tasks:
    val uncompletedToday = todayTasks.filter { !it.task.isCompleted }
    val completedToday = todayTasks.filter { it.task.isCompleted }

    // Partition uncompleted today: time-blocked vs untimed
    val scheduledTimelineTasks = uncompletedToday.filter { !it.task.dueTime.isNullOrBlank() }
    val untimedTasks = uncompletedToday.filter { it.task.dueTime.isNullOrBlank() }
        .sortedWith(compareByDescending<TaskWithSubtasks> { it.task.priority.ordinal }.thenBy { it.task.id })

    // Progress
    val totalTodayCount = todayTasks.size
    val doneCount = completedToday.size
    val progressRate = if (totalTodayCount > 0) doneCount.toFloat() / totalTodayCount else 0f

    // Next action spotlight ("What should I do right now?")
    val nextActionTask = scheduledTimelineTasks.firstOrNull() ?: untimedTasks.firstOrNull()

    // Collapse state
    var isOverdueExpanded by remember { mutableStateOf(true) }
    var isCompletedExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("today_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header: Greeting, Date, Progress Ring
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = Color(0xFF6366F1)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = DateTimeUtils.getGreeting(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = DateTimeUtils.formatHeaderDate(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (totalTodayCount == 0) "No tasks yet today" else "$doneCount of $totalTodayCount tasks done",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    // Progress Ring
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val strokeColor = MaterialTheme.colorScheme.primary
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 5.dp.toPx()
                            val diameter = size.minDimension - strokeWidth
                            val arcSize = Size(diameter, diameter)
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )
                            drawArc(
                                color = strokeColor,
                                startAngle = -90f,
                                sweepAngle = 360f * progressRate,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Text(
                            text = "${(progressRate * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. Smart Suggestion / "Plan my day" helper (Dismissible)
        if (!isSuggestionDismissed) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Focus Tip: Tap the play icon next to any task to enter calm Focus Mode with a 25m Pomodoro.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.isSmartSuggestionDismissed.value = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss tip",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. "What should I do right now?" Hero Spotlight
        if (nextActionTask != null) {
            item {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("right_now_hero_card"),
                    accentColor = Color(0xFF6366F1)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Up Next",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextActionTask.task.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            if (!nextActionTask.task.dueTime.isNullOrBlank()) {
                                Text(
                                    text = "Scheduled at ${DateTimeUtils.formatTime12H(nextActionTask.task.dueTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.openFocusMode(nextActionTask.task) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("hero_focus_btn")
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Focus")
                        }
                    }
                }
            }
        }

        // 4. Overdue Section (Collapsible)
        if (overdueTasks.isNotEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isOverdueExpanded = !isOverdueExpanded },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Overdue (${overdueTasks.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { viewModel.moveAllOverdueToToday() },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Move to today", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, maxLines = 1)
                                }
                                Icon(
                                    imageVector = if (isOverdueExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = isOverdueExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                overdueTasks.forEach { ot ->
                                    TaskRowItem(
                                        taskWithSubtasks = ot,
                                        isOverdueMode = true,
                                        onToggleComplete = { viewModel.toggleTaskComplete(ot.task) },
                                        onClick = { viewModel.selectedTaskForDetail.value = ot },
                                        onStartFocus = { viewModel.openFocusMode(ot.task) },
                                        onReschedule = { viewModel.taskToReschedule.value = ot.task },
                                        onDelete = { viewModel.deleteTask(ot.task.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Scheduled Timeline (Time-blocked)
        if (scheduledTimelineTasks.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scheduled Timeline",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${scheduledTimelineTasks.size} time blocks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TimeBlockingTimeline(
                        timedTasks = scheduledTimelineTasks,
                        onTaskClick = { viewModel.selectedTaskForDetail.value = it },
                        onStartFocus = { viewModel.openFocusMode(it.task) }
                    )
                }
            }
        }

        // 7. To-Do Today (Untimed tasks) - ABOVE Routines
        item {
            Text(
                text = "To-Do Today (${untimedTasks.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (untimedTasks.isEmpty() && scheduledTimelineTasks.isEmpty()) {
            // Calm Empty State
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🌱", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Clear day ahead",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add something small or enjoy the space.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.openCreateTask() }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Capture a task")
                        }
                    }
                }
            }
        } else {
            items(untimedTasks, key = { it.task.id }) { item ->
                TaskRowItem(
                    taskWithSubtasks = item,
                    onToggleComplete = { viewModel.toggleTaskComplete(item.task) },
                    onClick = { viewModel.selectedTaskForDetail.value = item },
                    onStartFocus = { viewModel.openFocusMode(item.task) },
                    onReschedule = { viewModel.taskToReschedule.value = item.task },
                    onDelete = { viewModel.deleteTask(item.task.id) }
                )
            }
        }

        // 8. Routines Due Now - BELOW To-Do list
        if (routines.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Routines",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    routines.take(2).forEach { routineItem ->
                        RoutineCard(
                            routineWithSteps = routineItem,
                            onRunRoutine = { viewModel.startRoutineRunner(routineItem) },
                            onToggleStep = { stepId, done -> viewModel.markRoutineStepDone(stepId, done) },
                            onDelete = { viewModel.deleteRoutine(routineItem.routine) },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }

        // 9. Completed Tasks (Collapsed by default)
        if (completedToday.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCompletedExpanded = !isCompletedExpanded },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Completed (${completedToday.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = if (isCompletedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(visible = isCompletedExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                completedToday.forEach { ct ->
                                    TaskRowItem(
                                        taskWithSubtasks = ct,
                                        onToggleComplete = { viewModel.toggleTaskComplete(ct.task) },
                                        onClick = { viewModel.selectedTaskForDetail.value = ct },
                                        onStartFocus = { viewModel.openFocusMode(ct.task) },
                                        onReschedule = { viewModel.taskToReschedule.value = ct.task },
                                        onDelete = { viewModel.deleteTask(ct.task.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
