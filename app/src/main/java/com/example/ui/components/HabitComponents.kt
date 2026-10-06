package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitWithLogs
import com.example.util.DateTimeUtils
import java.time.LocalDate

@Composable
fun HabitTodayChipRow(
    habitsWithLogs: List<HabitWithLogs>,
    onToggleHabit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (habitsWithLogs.isEmpty()) return
    val todayStr = DateTimeUtils.todayStr()

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habits_today_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(habitsWithLogs, key = { it.habit.id }) { item ->
            val habit = item.habit
            val isCompletedToday = item.logs.any { it.date == todayStr && it.isCompleted }
            val (currentStreak, _) = DateTimeUtils.calculateHabitStreaks(item.logs, habit.graceDaysPerWeek)
            val habitColor = Color(habit.colorHex)

            val chipBg by animateColorAsState(
                targetValue = if (isCompletedToday) habitColor else MaterialTheme.colorScheme.surface,
                label = "habit_chip_bg"
            )
            val chipTextColor by animateColorAsState(
                targetValue = if (isCompletedToday) Color.White else MaterialTheme.colorScheme.onSurface,
                label = "habit_chip_text"
            )

            Surface(
                modifier = Modifier
                    .testTag("habit_chip_${habit.id}")
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onToggleHabit(habit.id) },
                shape = RoundedCornerShape(20.dp),
                color = chipBg,
                tonalElevation = if (isCompletedToday) 4.dp else 1.dp,
                shadowElevation = if (isCompletedToday) 2.dp else 0.5.dp,
                border = if (!isCompletedToday) {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                } else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = habit.iconEmoji,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = chipTextColor
                    )
                    if (currentStreak > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${currentStreak}🔥",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompletedToday) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                        )
                    }
                    if (isCompletedToday) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitDetailCard(
    habitWithLogs: HabitWithLogs,
    onToggleDate: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithLogs.habit
    val logs = habitWithLogs.logs
    val habitColor = Color(habit.colorHex)
    val today = LocalDate.now()
    val (currentStreak, bestStreak) = DateTimeUtils.calculateHabitStreaks(logs, habit.graceDaysPerWeek)

    // Calculate completions in past 7 days
    val past7Days = (0..6).map { today.minusDays(it.toLong()).toString() }
    val completedThisWeek = logs.count { it.date in past7Days && it.isCompleted }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Emoji + Title + Streaks + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(habitColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = habit.iconEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$completedThisWeek/${habit.targetDaysPerWeek} days this week · Grace: ${habit.graceDaysPerWeek}d/wk",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete habit",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Streaks badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${currentStreak} days",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Current Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${bestStreak} days",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Best Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 30-day dot matrix calendar
            Text(
                text = "Last 30 Days (Past & Today only):",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 30 days grid (5 rows x 6 columns or 3 rows x 10 cols)
            val last30Days = (29 downTo 0).map { today.minusDays(it.toLong()) }
            val completedDatesSet = logs.filter { it.isCompleted }.map { it.date }.toSet()

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                last30Days.chunked(10).forEach { rowDays ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowDays.forEach { date ->
                            val dStr = date.toString()
                            val isDone = completedDatesSet.contains(dStr)
                            val isToday = date == today
                            val isFuture = date.isAfter(today)

                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDone) habitColor else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .then(
                                        if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        else Modifier
                                    )
                                    .then(
                                        if (!isFuture) Modifier.clickable { onToggleDate(dStr) }
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
