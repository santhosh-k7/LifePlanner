package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material.icons.outlined.ViewWeek
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
import com.example.data.model.TaskWithSubtasks
import com.example.ui.components.TaskRowItem
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.util.DateTimeUtils
import java.time.LocalDate

@Composable
fun UpcomingScreen(
    viewModel: LifePlannerViewModel,
    modifier: Modifier = Modifier
) {
    val mode by viewModel.upcomingViewMode.collectAsState() // 0: List, 1: Weekly Planner
    val allTasks by viewModel.allTasks.collectAsState()
    val today = LocalDate.now()

    var selectedWeeklyDay by remember { mutableStateOf(today.plusDays(1)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("upcoming_screen")
    ) {
        // Mode Selector Tab Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Segmented Button Toggle
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = mode == 0,
                    onClick = { viewModel.upcomingViewMode.value = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {}
                ) {
                    Icon(Icons.Outlined.ViewDay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("List", fontSize = 12.sp)
                }

                SegmentedButton(
                    selected = mode == 1,
                    onClick = { viewModel.upcomingViewMode.value = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {}
                ) {
                    Icon(Icons.Outlined.ViewWeek, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Planner", fontSize = 12.sp)
                }
            }
        }

        if (mode == 0) {
            // LIST MODE: Next 14 days grouped
            val next14Days = DateTimeUtils.getUpcomingDays(14).drop(1) // from Tomorrow onward

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("upcoming_list_view"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                next14Days.forEach { date ->
                    val dateStr = date.toString()
                    val tasksForDate = allTasks.filter { it.task.dueDate == dateStr }

                    item {
                        Column {
                            // Day Header with + Add button for that day
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = DateTimeUtils.formatDisplayDate(dateStr),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "${tasksForDate.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.openQuickAdd(initialDate = dateStr) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add task for this day",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (tasksForDate.isEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No tasks planned",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    tasksForDate.forEach { taskItem ->
                                        TaskRowItem(
                                            taskWithSubtasks = taskItem,
                                            onToggleComplete = { viewModel.toggleTaskComplete(taskItem.task) },
                                            onClick = { viewModel.selectedTaskForDetail.value = taskItem },
                                            onStartFocus = { viewModel.openFocusMode(taskItem.task) },
                                            onReschedule = { viewModel.taskToReschedule.value = taskItem.task },
                                            onDelete = { viewModel.deleteTask(taskItem.task.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // WEEKLY PLANNER MODE
            val weekDays = (0..6).map { today.plusDays(it.toLong()) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("upcoming_planner_view")
                    .padding(horizontal = 16.dp)
            ) {
                // 7-day strip with load indicators
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(weekDays) { date ->
                        val dateStr = date.toString()
                        val count = allTasks.count { it.task.dueDate == dateStr && !it.task.isCompleted }
                        val isSelected = date == selectedWeeklyDay

                        val loadColor = when {
                            count == 0 -> Color.Transparent
                            count <= 2 -> Color(0xFF10B981) // light green
                            count <= 4 -> Color(0xFFF59E0B) // amber
                            else -> Color(0xFFEF4444) // heavy red
                        }

                        Card(
                            modifier = Modifier
                                .width(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedWeeklyDay = date }
                                .then(
                                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                                    else Modifier
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = DateTimeUtils.formatShortDay(dateStr),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(loadColor)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detail for the selected day in Weekly Planner
                val selectedDayStr = selectedWeeklyDay.toString()
                val selectedDayTasks = allTasks.filter { it.task.dueDate == selectedDayStr }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = DateTimeUtils.formatDisplayDate(selectedDayStr),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val loadLabel = when {
                            selectedDayTasks.isEmpty() -> "Clear schedule"
                            selectedDayTasks.size <= 2 -> "Light workload"
                            selectedDayTasks.size <= 4 -> "Moderate workload"
                            else -> "Heavy workload"
                        }
                        Text(
                            text = "$loadLabel · ${selectedDayTasks.size} tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.openQuickAdd(initialDate = selectedDayStr) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedDayTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🌤️", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nothing scheduled for this day",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(selectedDayTasks, key = { it.task.id }) { item ->
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
                }
            }
        }
    }
}
