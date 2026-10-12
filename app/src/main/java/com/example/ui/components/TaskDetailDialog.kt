package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Category
import com.example.data.model.Priority
import com.example.data.model.RepeatRule
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskWithSubtasks
import com.example.ui.theme.*
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailDialog(
    taskWithSubtasks: TaskWithSubtasks,
    viewModel: LifePlannerViewModel,
    onDismiss: () -> Unit
) {
    val task = taskWithSubtasks.task
    val subtasks = taskWithSubtasks.subtasks

    var title by remember { mutableStateOf(task.title) }
    var notes by remember { mutableStateOf(task.notes) }
    var dueDate by remember { mutableStateOf(task.dueDate) }
    var dueTime by remember { mutableStateOf(task.dueTime ?: "") }
    var durationMin by remember { mutableIntStateOf(task.durationMin) }
    var priority by remember { mutableStateOf(task.priority) }
    var categoryId by remember { mutableStateOf(task.categoryId) }
    var repeatRule by remember { mutableStateOf(task.repeatRule) }
    var selectedReminderOffsets by remember { mutableStateOf(task.getReminderOffsetList().toSet()) }

    var newSubtaskText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("task_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header (No overlap)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Task",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Date & Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Due Date (dd-MM-yyyy)", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )

                        OutlinedTextField(
                            value = dueTime,
                            onValueChange = { dueTime = it },
                            label = { Text("Time (e.g. 14:30)", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    // Multi-notification Selector for Events & Tasks
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (selectedReminderOffsets.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Event & Task Notifications",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Select multiple notification timings (before event and at time of event).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Combos
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedReminderOffsets == setOf(0, 15),
                                onClick = { selectedReminderOffsets = setOf(0, 15) },
                                label = { Text("At time & 15m before", fontSize = 11.sp) },
                                leadingIcon = if (selectedReminderOffsets == setOf(0, 15)) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = selectedReminderOffsets == setOf(0, 30),
                                onClick = { selectedReminderOffsets = setOf(0, 30) },
                                label = { Text("At time & 30m before", fontSize = 11.sp) },
                                leadingIcon = if (selectedReminderOffsets == setOf(0, 30)) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = selectedReminderOffsets.isEmpty(),
                                onClick = { selectedReminderOffsets = emptySet() },
                                label = { Text("7 AM morning only", fontSize = 11.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            listOf(
                                0 to "🔔 At event time",
                                5 to "5 mins before",
                                10 to "10 mins before",
                                15 to "15 mins before",
                                30 to "30 mins before",
                                60 to "1 hour before",
                                120 to "2 hours before",
                                1440 to "1 day before"
                            ).forEach { (offset, label) ->
                                val isSelected = selectedReminderOffsets.contains(offset)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedReminderOffsets = if (isSelected) {
                                            selectedReminderOffsets - offset
                                        } else {
                                            selectedReminderOffsets + offset
                                        }
                                    },
                                    label = { Text(label, fontSize = 11.sp) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedReminderOffsets.isNotEmpty()) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (selectedReminderOffsets.isNotEmpty()) {
                                    val labels = selectedReminderOffsets.sorted().map {
                                        when (it) {
                                            0 -> "At event time"
                                            1440 -> "1d before"
                                            60 -> "1h before"
                                            120 -> "2h before"
                                            else -> "${it}m before"
                                        }
                                    }
                                    "Active (${selectedReminderOffsets.size}): ${labels.joinToString(", ")}"
                                } else {
                                    "Default: Morning reminder at 7:00 AM on task day"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedReminderOffsets.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Duration Minutes for Time Blocking
                    Column {
                        Text(
                            text = "Time-block Duration: ${if (durationMin > 0) "$durationMin minutes" else "None"}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf(0, 15, 30, 45, 60, 90).forEach { mins ->
                                FilterChip(
                                    selected = durationMin == mins,
                                    onClick = { durationMin = mins },
                                    label = { Text(if (mins == 0) "None" else "${mins}m", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Priority Row
                    Column {
                        Text("Priority", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Priority.values().forEach { p ->
                                FilterChip(
                                    selected = priority == p,
                                    onClick = { priority = p },
                                    label = { Text(p.label, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Flag,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = when (p) {
                                                Priority.HIGH -> PriorityHigh
                                                Priority.MEDIUM -> PriorityMedium
                                                Priority.LOW -> PriorityLow
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Category Row (Including "Others" explicitly)
                    Column {
                        Text("Category", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf("work", "personal", "health", "learning", "others").forEach { cat ->
                                val catColor = Category.getColor(cat)
                                FilterChip(
                                    selected = categoryId.lowercase() == cat.lowercase() || (cat == "others" && categoryId.lowercase() == "other"),
                                    onClick = { categoryId = cat },
                                    label = {
                                        Text(
                                            Category.getDisplayName(cat),
                                            fontSize = 12.sp,
                                            color = if (categoryId.lowercase() == cat.lowercase()) MaterialTheme.colorScheme.onSurface else catColor
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Repeat Rule Row
                    Column {
                        Text("Recurring & Repeat", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            RepeatRule.values().forEach { r ->
                                FilterChip(
                                    selected = repeatRule == r,
                                    onClick = { repeatRule = r },
                                    label = { Text(r.label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Subtasks Checklist Section
                    Column {
                        Text(
                            text = "Checklist Subtasks (${subtasks.count { it.isDone }}/${subtasks.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        subtasks.forEach { st ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = st.isDone,
                                    onCheckedChange = { viewModel.toggleSubtask(st) }
                                )
                                Text(
                                    text = st.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.deleteSubtask(st) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = "Delete subtask",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Add new subtask input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSubtaskText,
                                onValueChange = { newSubtaskText = it },
                                placeholder = { Text("Add subtask...", fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalIconButton(
                                onClick = {
                                    if (newSubtaskText.isNotBlank()) {
                                        viewModel.addSubtask(task.id, newSubtaskText)
                                        newSubtaskText = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add subtask")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Buttons: Delete, Cancel, Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            viewModel.deleteTask(task.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val offsetsString = selectedReminderOffsets.sorted().joinToString(",")
                            viewModel.updateTaskDetails(
                                task.copy(
                                    title = title.trim(),
                                    notes = notes.trim(),
                                    dueDate = dueDate.trim(),
                                    dueTime = dueTime.trim().ifEmpty { null },
                                    durationMin = durationMin,
                                    priority = priority,
                                    categoryId = categoryId,
                                    medium = "",
                                    repeatRule = repeatRule,
                                    reminderOffsetMin = selectedReminderOffsets.firstOrNull(),
                                    reminderOffsets = offsetsString
                                )
                            )
                            onDismiss()
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
