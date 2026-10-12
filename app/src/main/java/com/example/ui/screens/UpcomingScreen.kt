package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskWithSubtasks
import com.example.ui.components.TaskRowItem
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.util.DateTimeUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun UpcomingScreen(
    viewModel: LifePlannerViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = when (themeMode) {
        0 -> androidx.compose.foundation.isSystemInDarkTheme()
        1 -> false
        else -> true
    }

    val allTasks by viewModel.allTasks.collectAsState()
    val today = remember { LocalDate.now() }

    // Calendar state: selected year and month
    var selectedYear by remember { mutableIntStateOf(today.year) }
    var selectedMonth by remember { mutableIntStateOf(today.monthValue) }
    var selectedDate by remember { mutableStateOf(today) }

    // View sub-modes: 0: Full Month Calendar, 1: 12-Month Year Overview, 2: Schedule List
    var calendarViewMode by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("upcoming_screen")
    ) {
        // Top Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Calendar & Upcoming",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Full Year $selectedYear · ${DateTimeUtils.formatDate(selectedDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick button to jump to today
            FilledTonalButton(
                onClick = {
                    selectedYear = today.year
                    selectedMonth = today.monthValue
                    selectedDate = today
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = "Jump to Today",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Today", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // View Mode Selector (Dedicated row with full width - perfect for Android phones)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            SegmentedButton(
                selected = calendarViewMode == 0,
                onClick = { calendarViewMode = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                icon = {}
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Month", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            SegmentedButton(
                selected = calendarViewMode == 1,
                onClick = { calendarViewMode = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                icon = {}
            ) {
                Icon(Icons.Outlined.ViewModule, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Year Grid", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            SegmentedButton(
                selected = calendarViewMode == 2,
                onClick = { calendarViewMode = 2 },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                icon = {}
            ) {
                Icon(Icons.Outlined.ViewDay, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("List", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Year Navigator Strip (< 2026 >)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    selectedYear -= 1
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Year", modifier = Modifier.size(18.dp))
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF6366F1).copy(alpha = 0.15f),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "$selectedYear",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6366F1),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }

            IconButton(
                onClick = {
                    selectedYear += 1
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Year", modifier = Modifier.size(18.dp))
            }
        }

        when (calendarViewMode) {
            0 -> {
                // MONTH CALENDAR VIEW (FULL YEAR NAVIGABLE)
                MonthCalendarView(
                    year = selectedYear,
                    month = selectedMonth,
                    selectedDate = selectedDate,
                    allTasks = allTasks,
                    isDark = isDark,
                    onMonthChange = { newM -> selectedMonth = newM },
                    onDateSelect = { newDate -> selectedDate = newDate },
                    onAddTask = { dateStr -> viewModel.openCreateTask(initialDate = dateStr) },
                    viewModel = viewModel
                )
            }
            1 -> {
                // 12-MONTH YEAR OVERVIEW GRID
                YearOverviewGridView(
                    year = selectedYear,
                    allTasks = allTasks,
                    isDark = isDark,
                    today = today,
                    onSelectMonth = { m ->
                        selectedMonth = m
                        calendarViewMode = 0
                    }
                )
            }
            2 -> {
                // SCHEDULE LIST MODE
                ScheduleListView(
                    allTasks = allTasks,
                    isDark = isDark,
                    onAddTask = { dateStr -> viewModel.openCreateTask(initialDate = dateStr) },
                    viewModel = viewModel
                )
            }
        }
    }
}

/**
 * Full Month Calendar with 12 Month Quick Bar and Selected Date Task Details
 */
@Composable
private fun MonthCalendarView(
    year: Int,
    month: Int,
    selectedDate: LocalDate,
    allTasks: List<TaskWithSubtasks>,
    isDark: Boolean,
    onMonthChange: (Int) -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    onAddTask: (String) -> Unit,
    viewModel: LifePlannerViewModel
) {
    val currentYm = YearMonth.of(year, month)
    val monthName = currentYm.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val today = LocalDate.now()

    // 12 Month Horizontal Selector Pills
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items((1..12).toList()) { m ->
            val mName = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val ym = YearMonth.of(year, m)
            // Count tasks in this month
            val countInMonth = allTasks.count {
                val d = DateTimeUtils.parseDate(it.task.dueDate)
                d.year == year && d.monthValue == m && !it.task.isCompleted
            }
            val isSelected = m == month

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF6366F1) else if (isDark) Color(0x33FFFFFF) else Color(0x15000000),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onMonthChange(m) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    if (countInMonth > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White.copy(alpha = 0.3f) else Color(0xFF6366F1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$countInMonth",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("month_calendar_scroll"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Calendar Card
        item {
            LiquidGlassCard(
                isDark = isDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Month title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$monthName $year",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    if (month == 1) {
                                        onMonthChange(12)
                                    } else {
                                        onMonthChange(month - 1)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month", modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = {
                                    if (month == 12) {
                                        onMonthChange(1)
                                    } else {
                                        onMonthChange(month + 1)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Weekday Names Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLabel ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Days of Month Matrix
                    val firstDayOfMonth = currentYm.atDay(1)
                    val daysInMonth = currentYm.lengthOfMonth()
                    // Monday is 1, Sunday is 7 -> offset is dayOfWeek.value - 1
                    val startOffset = firstDayOfMonth.dayOfWeek.value - 1

                    val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7

                    for (row in 0 until (totalCells / 7)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (col in 0 until 7) {
                                val cellIdx = row * 7 + col
                                val dayNum = cellIdx - startOffset + 1

                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dayNum in 1..daysInMonth) {
                                        val cellDate = LocalDate.of(year, month, dayNum)
                                        val isSelected = cellDate == selectedDate
                                        val isCurrentDay = cellDate == today
                                        val tasksOnDay = allTasks.filter {
                                            val td = DateTimeUtils.parseDate(it.task.dueDate)
                                            td == cellDate
                                        }
                                        val uncompletedCount = tasksOnDay.count { !it.task.isCompleted }

                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .then(
                                                    when {
                                                        isSelected -> Modifier.background(Color(0xFF6366F1))
                                                        isCurrentDay -> Modifier.border(1.5.dp, Color(0xFF6366F1), RoundedCornerShape(10.dp))
                                                        else -> Modifier
                                                    }
                                                )
                                                .clickable { onDateSelect(cellDate) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "$dayNum",
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected || isCurrentDay) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )

                                                // Task dot indicator
                                                if (uncompletedCount > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.White else Color(0xFF6366F1))
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Empty cell
                                        Spacer(modifier = Modifier.size(36.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Task Section
        val selectedDateStr = DateTimeUtils.formatDate(selectedDate)
        val tasksForSelected = allTasks.filter {
            val d = DateTimeUtils.parseDate(it.task.dueDate)
            d == selectedDate
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "Tasks for $selectedDateStr",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${tasksForSelected.size} scheduled · ${tasksForSelected.count { it.task.isCompleted }} completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { onAddTask(selectedDateStr) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (tasksForSelected.isEmpty()) {
            item {
                LiquidGlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tasks on $selectedDateStr",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add Task' above to schedule tasks for this date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(tasksForSelected, key = { it.task.id }) { taskItem ->
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

/**
 * 12-Month Year Overview Grid: Visualizes task density across the entire year
 */
@Composable
private fun YearOverviewGridView(
    year: Int,
    allTasks: List<TaskWithSubtasks>,
    isDark: Boolean,
    today: LocalDate,
    onSelectMonth: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("year_overview_grid"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Full Year $year Overview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tap any month card to view its interactive days & scheduled tasks.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Render 12 months as pairs in rows (2 per row)
        val months = (1..12).toList()
        items(months.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { m ->
                    val ym = YearMonth.of(year, m)
                    val mName = ym.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    val tasksInMonth = allTasks.filter {
                        val d = DateTimeUtils.parseDate(it.task.dueDate)
                        d.year == year && d.monthValue == m
                    }
                    val uncompleted = tasksInMonth.count { !it.task.isCompleted }

                    Box(modifier = Modifier.weight(1f)) {
                        LiquidGlassCard(
                            isDark = isDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onSelectMonth(m) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = mName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                                    )
                                    if (uncompleted > 0) {
                                        Surface(
                                            color = Color(0xFF6366F1),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "$uncompleted",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Mini Month Calendar Matrix
                                val firstDay = ym.atDay(1)
                                val daysInM = ym.lengthOfMonth()
                                val startOffset = firstDay.dayOfWeek.value - 1

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    for (r in 0 until 5) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            for (c in 0 until 7) {
                                                val dNum = r * 7 + c - startOffset + 1
                                                if (dNum in 1..daysInM) {
                                                    val isCurrent = (year == today.year && m == today.monthValue && dNum == today.dayOfMonth)
                                                    val hasTasks = tasksInMonth.any {
                                                        DateTimeUtils.parseDate(it.task.dueDate).dayOfMonth == dNum
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                when {
                                                                    isCurrent -> Color(0xFF6366F1)
                                                                    hasTasks -> Color(0xFF6366F1).copy(alpha = 0.4f)
                                                                    else -> Color.Transparent
                                                                }
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "$dNum",
                                                            fontSize = 8.sp,
                                                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // If odd number of months in last pair
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Schedule List View (Chronological next upcoming tasks grouped by dd-MM-yyyy)
 */
@Composable
private fun ScheduleListView(
    allTasks: List<TaskWithSubtasks>,
    isDark: Boolean,
    onAddTask: (String) -> Unit,
    viewModel: LifePlannerViewModel
) {
    val next14Days = DateTimeUtils.getUpcomingDays(30).drop(1)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("upcoming_list_view"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        next14Days.forEach { date ->
            val dateStr = DateTimeUtils.formatDate(date)
            val tasksForDate = allTasks.filter {
                val td = DateTimeUtils.parseDate(it.task.dueDate)
                td == date
            }

            item {
                Column {
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
                            onClick = { onAddTask(dateStr) },
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
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
}
