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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Category
import com.example.data.model.Review
import com.example.ui.components.TaskRowItem
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.util.DateTimeUtils
import java.time.LocalDate

@Composable
fun ReviewScreen(
    viewModel: LifePlannerViewModel,
    modifier: Modifier = Modifier
) {
    var subSection by remember { mutableIntStateOf(0) } // 0: Daily Review, 1: Weekly Review, 2: Progress & Journal

    val todayTasks by viewModel.todayTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val reviews by viewModel.allReviews.collectAsState()
    val totalFocusSec by viewModel.totalFocusSeconds.collectAsState()

    val completedToday = todayTasks.filter { it.task.isCompleted }
    val leftoversToday = todayTasks.filter { !it.task.isCompleted }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("review_screen")
    ) {
        // Top Header & Sub-section Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reflection & Review",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 3-way Segmented Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = subSection == 0,
                    onClick = { subSection = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    icon = {}
                ) {
                    Text("Daily", fontSize = 12.sp)
                }
                SegmentedButton(
                    selected = subSection == 1,
                    onClick = { subSection = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    icon = {}
                ) {
                    Text("Weekly", fontSize = 12.sp)
                }
                SegmentedButton(
                    selected = subSection == 2,
                    onClick = { subSection = 2 },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    icon = {}
                ) {
                    Text("Progress", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (subSection) {
            0 -> {
                // SECTION 1: DAILY REVIEW
                val reflectionText by viewModel.dailyReviewReflection.collectAsState()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Daily Close-the-Loop",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Celebrate what got done today and gently triage leftovers.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${completedToday.size}",
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("Completed", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${leftoversToday.size}",
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text("Leftover", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

                    // Leftovers Triage
                    if (leftoversToday.isNotEmpty()) {
                        item {
                            Text(
                                text = "Leftovers (${leftoversToday.size}) · Quick Action",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        items(leftoversToday, key = { it.task.id }) { item ->
                            val task = item.task
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val tomorrowStr = LocalDate.now().plusDays(1).toString()
                                        FilledTonalButton(
                                            onClick = { viewModel.rescheduleTask(task.id, tomorrowStr) },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Tomorrow", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.taskToReschedule.value = task },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Reschedule", fontSize = 11.sp)
                                        }

                                        TextButton(
                                            onClick = { viewModel.deleteTask(task.id) },
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Drop", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Reflection note input
                    item {
                        Column {
                            Text(
                                text = "Evening Reflection",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = reflectionText,
                                onValueChange = { viewModel.dailyReviewReflection.value = it },
                                placeholder = { Text("What went well today? (e.g. Focused 45 mins on deep work)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.submitDailyReview() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_daily_review_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Complete Daily Review")
                            }
                        }
                    }
                }
            }

            1 -> {
                // SECTION 2: WEEKLY REVIEW (Guided 4-step wizard)
                val step by viewModel.weeklyReviewStep.collectAsState()
                val wellText by viewModel.weeklyReviewWell.collectAsState()
                val improveText by viewModel.weeklyReviewImprove.collectAsState()
                val p1 by viewModel.weeklyPriority1.collectAsState()
                val p2 by viewModel.weeklyPriority2.collectAsState()
                val p3 by viewModel.weeklyPriority3.collectAsState()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Wizard step progress bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Weekly Review Flow",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Step $step of 4",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { step.toFloat() / 4f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    when (step) {
                        1 -> {
                            // Step 1: Stats
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text(
                                            text = "1. Weekly Performance Summary",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        val completedTotal = allTasks.count { it.task.isCompleted }
                                        val totalCount = allTasks.size
                                        val rate = if (totalCount > 0) (completedTotal * 100) / totalCount else 100
                                        val focusHours = (totalFocusSec ?: 0L) / 3600

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "$completedTotal / $totalCount",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "Completed",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1
                                                )
                                            }

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "$rate%",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    "Rate",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1
                                                )
                                            }

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "${focusHours}h ${(totalFocusSec ?: 0L) % 3600 / 60}m",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    "Focus",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Button(
                                            onClick = { viewModel.weeklyReviewStep.value = 2 },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Next: What went well? →")
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Step 2: What went well?
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text(
                                            text = "2. What went well this week?",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Acknowledge progress, consistent routines, or great focus sessions.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        OutlinedTextField(
                                            value = wellText,
                                            onValueChange = { viewModel.weeklyReviewWell.value = it },
                                            placeholder = { Text("e.g. Consistent morning resets and completed quarterly deck.") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 4,
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = { viewModel.weeklyReviewStep.value = 1 },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Back")
                                            }
                                            Button(
                                                onClick = { viewModel.weeklyReviewStep.value = 3 },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Next →")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            // Step 3: What didn't, and why?
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text(
                                            text = "3. What didn't go well, and why?",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Identify friction without self-judgment: Over-planning? Distractions? Fatigue?",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        OutlinedTextField(
                                            value = improveText,
                                            onValueChange = { viewModel.weeklyReviewImprove.value = it },
                                            placeholder = { Text("e.g. Took on too many meetings on Wednesday afternoon.") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 4,
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = { viewModel.weeklyReviewStep.value = 2 },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Back")
                                            }
                                            Button(
                                                onClick = { viewModel.weeklyReviewStep.value = 4 },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Next →")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        4 -> {
                            // Step 4: Pick up to 3 priorities for next week
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text(
                                            text = "4. Next Week's Top 3 Priorities",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "These will automatically be created and pinned for next week.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        OutlinedTextField(
                                            value = p1,
                                            onValueChange = { viewModel.weeklyPriority1.value = it },
                                            label = { Text("Priority 1") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = p2,
                                            onValueChange = { viewModel.weeklyPriority2.value = it },
                                            label = { Text("Priority 2") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = p3,
                                            onValueChange = { viewModel.weeklyPriority3.value = it },
                                            label = { Text("Priority 3 (optional)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(18.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = { viewModel.weeklyReviewStep.value = 3 },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Back")
                                            }
                                            Button(
                                                onClick = { viewModel.submitWeeklyReview() },
                                                modifier = Modifier
                                                    .weight(1.5f)
                                                    .testTag("submit_weekly_review_btn")
                                            ) {
                                                Text("Complete Review 🎉")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // SECTION 3: PROGRESS OVERVIEW & JOURNAL
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Last 7 Days Completion Bar Chart
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Last 7 Days Activity",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val today = LocalDate.now()
                                val daysList = (6 downTo 0).map { today.minusDays(it.toLong()) }
                                val primaryColor = MaterialTheme.colorScheme.primary

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    daysList.forEach { date ->
                                        val dStr = date.toString()
                                        val count = allTasks.count { it.task.completedAt != null && it.task.dueDate == dStr }
                                        val maxCount = 6
                                        val barFraction = minOf(1f, (count.toFloat() / maxCount).coerceAtLeast(0.1f))

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (count > 0) "$count" else "",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryColor
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(18.dp)
                                                    .height((barFraction * 80).dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (count > 0) primaryColor else MaterialTheme.colorScheme.surfaceVariant)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = DateTimeUtils.formatShortDay(dStr),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Category Breakdown
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Completed by Category",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val categories = listOf("work", "personal", "health", "learning", "other")
                                categories.forEach { catId ->
                                    val count = allTasks.count { it.task.categoryId == catId && it.task.isCompleted }
                                    val catColor = Category.getColor(catId)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = catId.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "$count done",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Past Reviews Journal
                    item {
                        Text(
                            text = "Reviews Journal (${reviews.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (reviews.isEmpty()) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No saved reviews yet. Complete your first daily or weekly review above!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(reviews, key = { it.id }) { review ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            color = if (review.reviewType == "DAILY") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "${review.reviewType} REVIEW",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (review.reviewType == "DAILY") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = DateTimeUtils.formatDisplayDate(review.date),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (review.reflectionWell.isNotBlank()) {
                                        Text(
                                            text = "What went well: ${review.reflectionWell}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    if (review.reflectionImprove.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Friction: ${review.reflectionImprove}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (review.nextWeekPriorities.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Priorities: ${review.nextWeekPriorities}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
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
}
