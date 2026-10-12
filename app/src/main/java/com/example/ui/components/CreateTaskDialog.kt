package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Priority
import com.example.data.model.RepeatRule
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.util.DateTimeUtils
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateTaskDialog(
    initialDate: String? = null,
    viewModel: LifePlannerViewModel,
    onDismiss: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = when (themeMode) {
        0 -> androidx.compose.foundation.isSystemInDarkTheme()
        1 -> false
        else -> true
    }

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(initialDate ?: DateTimeUtils.todayStr()) }
    var dueTime by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("work") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedReminderOffsets by remember { mutableStateOf(setOf(0, 15)) } // Default: at event time & 15m before
    var durationMin by remember { mutableIntStateOf(30) }
    var repeatRule by remember { mutableStateOf(RepeatRule.NONE) }

    // Subtasks
    val subtasks = remember { mutableStateListOf<String>() }
    var newSubtaskText by remember { mutableStateOf("") }

    val categoryPresets = listOf(
        Triple("work", "Work", Color(0xFF3B82F6)),
        Triple("coding", "Coding", Color(0xFF6366F1)),
        Triple("personal", "Personal", Color(0xFF10B981)),
        Triple("study", "Study", Color(0xFF8B5CF6)),
        Triple("health", "Health", Color(0xFFEC4899)),
        Triple("finance", "Finance", Color(0xFFF59E0B)),
        Triple("projects", "Projects", Color(0xFF06B6D4)),
        Triple("other", "Others", Color(0xFF64748B))
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 18.dp)
                .testTag("create_task_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.96f),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "New Task",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Fill all details for scheduling & notifications",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Task Name
                    LiquidGlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Task Name *",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = { Text("What needs to be accomplished?") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("task_name_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }

                    // 2. Deadline & Time
                    LiquidGlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Deadline & Time",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Date Field formatted dd-MM-yyyy
                            OutlinedTextField(
                                value = dueDate,
                                onValueChange = { dueDate = it },
                                label = { Text("Deadline Date (dd-MM-yyyy)") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("task_deadline_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Quick Date Selector Chips
                            Spacer(modifier = Modifier.height(6.dp))
                            val today = LocalDate.now()
                            val todayStr = DateTimeUtils.formatDate(today)
                            val tomorrowStr = DateTimeUtils.formatDate(today.plusDays(1))
                            val in2DaysStr = DateTimeUtils.formatDate(today.plusDays(2))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = dueDate == todayStr,
                                    onClick = { dueDate = todayStr },
                                    label = { Text("Today ($todayStr)", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = dueDate == tomorrowStr,
                                    onClick = { dueDate = tomorrowStr },
                                    label = { Text("Tomorrow ($tomorrowStr)", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = dueDate == in2DaysStr,
                                    onClick = { dueDate = in2DaysStr },
                                    label = { Text("In 2 Days ($in2DaysStr)", fontSize = 11.sp) }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Time Field (HH:mm)
                            OutlinedTextField(
                                value = dueTime,
                                onValueChange = { dueTime = it },
                                label = { Text("Time (e.g. 14:30 or 09:00)") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    if (dueTime.isNotBlank()) {
                                        Text(
                                            text = DateTimeUtils.formatTime12H(dueTime),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("task_time_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Quick Time Presets
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "09:00" to "Morning 9 AM",
                                    "13:00" to "1 PM",
                                    "15:30" to "3:30 PM",
                                    "18:00" to "Evening 6 PM",
                                    "20:00" to "Night 8 PM",
                                    "" to "Clear Time"
                                ).forEach { (timeVal, label) ->
                                    FilterChip(
                                        selected = dueTime == timeVal && timeVal.isNotEmpty(),
                                        onClick = { dueTime = timeVal },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Category
                    LiquidGlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categoryPresets.forEach { (catId, catLabel, catColor) ->
                                    val isSelected = categoryId == catId
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { categoryId = catId },
                                        label = { Text(catLabel, fontSize = 12.sp) },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(catColor)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Notification Timings (Multi-notification support for events)
                    LiquidGlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Event & Task Notifications",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Select multiple timings: notify before event and at time of event.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

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
                                    selected = selectedReminderOffsets == setOf(0, 15, 60),
                                    onClick = { selectedReminderOffsets = setOf(0, 15, 60) },
                                    label = { Text("1h, 15m & At time", fontSize = 11.sp) },
                                    leadingIcon = if (selectedReminderOffsets == setOf(0, 15, 60)) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null
                                )
                                FilterChip(
                                    selected = selectedReminderOffsets.isEmpty(),
                                    onClick = { selectedReminderOffsets = emptySet() },
                                    label = { Text("7 AM morning only", fontSize = 11.sp) }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Multi-select Individual Timing Chips
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
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

                            Spacer(modifier = Modifier.height(8.dp))

                            // Status Summary Banner
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedReminderOffsets.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (selectedReminderOffsets.isNotEmpty()) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsNone,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (selectedReminderOffsets.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
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
                                            "Active: ${labels.joinToString(", ")}"
                                        } else {
                                            "Default: Morning notification at 7:00 AM on task day"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selectedReminderOffsets.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    // 6. Priority & Time Blocking
                    LiquidGlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Priority",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Priority.values().forEach { p ->
                                    val isSelected = priority == p
                                    val pColor = when (p) {
                                        Priority.HIGH -> Color(0xFFEF4444)
                                        Priority.MEDIUM -> Color(0xFFF59E0B)
                                        Priority.LOW -> Color(0xFF10B981)
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { priority = p },
                                        label = { Text(p.label, fontSize = 12.sp) },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(pColor)
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Estimated Duration / Time-block",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(0, 15, 30, 45, 60, 90, 120).forEach { mins ->
                                    FilterChip(
                                        selected = durationMin == mins,
                                        onClick = { durationMin = mins },
                                        label = { Text(if (mins == 0) "None" else "${mins}m", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // 7. Subtasks Checklist
                    LiquidGlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Subtasks / Steps (${subtasks.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // List existing subtasks
                            subtasks.forEachIndexed { index, st ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = st, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    IconButton(
                                        onClick = { subtasks.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newSubtaskText,
                                    onValueChange = { newSubtaskText = it },
                                    placeholder = { Text("Add subtask step...", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        if (newSubtaskText.isNotBlank()) {
                                            subtasks.add(newSubtaskText.trim())
                                            newSubtaskText = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text("Add", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // 8. Notes & Repeat Schedule
                    LiquidGlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Repeat Schedule",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                RepeatRule.values().forEach { rule ->
                                    FilterChip(
                                        selected = repeatRule == rule,
                                        onClick = { repeatRule = rule },
                                        label = { Text(rule.label, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Notes / Description",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                placeholder = { Text("Additional context, links, details...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Save Action
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.createFullTask(
                                title = title,
                                notes = notes,
                                dueDate = dueDate,
                                dueTime = dueTime,
                                durationMin = durationMin,
                                priority = priority,
                                categoryId = categoryId,
                                reminderOffsets = selectedReminderOffsets.sorted(),
                                repeatRule = repeatRule,
                                subtaskTitles = subtasks.toList()
                            )
                            onDismiss()
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_new_task_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Task",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
