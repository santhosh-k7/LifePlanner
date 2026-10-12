package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeetCodeProblem
import com.example.ui.theme.*
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeetCodeScreen(
    viewModel: LifePlannerViewModel,
    modifier: Modifier = Modifier
) {
    val problems by viewModel.allLeetCodeProblems.collectAsState()
    val showLogModal by viewModel.showLogLeetCodeSheet.collectAsState()

    val totalSolved = problems.size
    val easyCount = remember(problems) { problems.count { it.difficulty.equals("Easy", ignoreCase = true) } }
    val mediumCount = remember(problems) { problems.count { it.difficulty.equals("Medium", ignoreCase = true) } }
    val hardCount = remember(problems) { problems.count { it.difficulty.equals("Hard", ignoreCase = true) } }

    val todayStr = remember { DateTimeUtils.todayStr() }
    val solvedTodayCount = remember(problems) { problems.count { it.dateSolved == todayStr } }

    // Unique active days for streak estimation
    val activeDaysCount = remember(problems) { problems.map { it.dateSolved }.distinct().size }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("leetcode_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Hero Card: Liquid Glass LeetCode Analytics
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = Color(0xFFF59E0B) // Amber/LeetCode orange
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "LeetCode Progress",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Master patterns, track insights & maintain consistency",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Daily Streak Badge
                            LiquidGlassPill(
                                text = "🔥 $activeDaysCount Days Active",
                                accentColor = Color(0xFFF97316),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Total Solved Metric
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "$totalSolved",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = (-1).sp
                                )
                                Text(
                                    text = "Total Solved",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Target / Today Solved Box
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$solvedTodayCount Today",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (solvedTodayCount > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (solvedTodayCount >= 2) "Target Met! 🎯" else "Daily Target: 2",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Difficulty Distribution Cards in Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Easy Card
                            DifficultyStatBox(
                                label = "Easy",
                                count = easyCount,
                                total = totalSolved,
                                accentColor = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )

                            // Medium Card
                            DifficultyStatBox(
                                label = "Medium",
                                count = mediumCount,
                                total = totalSolved,
                                accentColor = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )

                            // Hard Card
                            DifficultyStatBox(
                                label = "Hard",
                                count = hardCount,
                                total = totalSolved,
                                accentColor = Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section Header: Recent Solved Questions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recently Solved (${problems.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Problems List
            if (problems.isEmpty()) {
                item {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "💡", fontSize = 36.sp)
                            Text(
                                text = "No problems logged yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Click '+ Log Problem' below to record a LeetCode problem, difficulty, and your key algorithm intuition.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(problems, key = { it.id }) { problem ->
                    LeetCodeProblemItem(
                        problem = problem,
                        onDelete = { viewModel.deleteLeetCodeProblem(problem) }
                    )
                }
            }
        }

        // Floating Action Button to Log Problem
        FloatingActionButton(
            onClick = { viewModel.openLogLeetCode() },
            containerColor = Color(0xFFF59E0B),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("fab_log_leetcode")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Problem")
                Text("Log Problem", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Modal Sheet to Log Problem
        if (showLogModal) {
            LogLeetCodeModal(
                onDismiss = { viewModel.showLogLeetCodeSheet.value = false },
                onSave = { title, num, diff, topic, notes, timeSpent ->
                    viewModel.logLeetCodeProblem(title, num, diff, topic, notes, timeSpent)
                }
            )
        }
    }
}

@Composable
fun DifficultyStatBox(
    label: String,
    count: Int,
    total: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val pct = if (total > 0) (count * 100) / total else 0

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$pct%",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LeetCodeProblemItem(
    problem: LeetCodeProblem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val diffColor = when (problem.difficulty.lowercase()) {
        "easy" -> Color(0xFF10B981)
        "medium" -> Color(0xFFF59E0B)
        "hard" -> Color(0xFFEF4444)
        else -> MaterialTheme.colorScheme.primary
    }

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("leetcode_problem_${problem.id}"),
        accentColor = diffColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Difficulty pill, Topic pill, and Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    LiquidGlassPill(
                        text = problem.difficulty.uppercase(),
                        accentColor = diffColor,
                        fontSize = 10.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = problem.topic,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (problem.timeSpentMinutes > 0) {
                        Text(
                            text = "⏱️ ${problem.timeSpentMinutes}m",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            val titleDisplay = if (problem.problemNumber != null) {
                "#${problem.problemNumber}. ${problem.title}"
            } else {
                problem.title
            }

            Text(
                text = titleDisplay,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Key Intuition / Algorithm Notes
            if (problem.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🧠 ${problem.notes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Solved on ${DateTimeUtils.formatDisplayDate(problem.dateSolved)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogLeetCodeModal(
    onDismiss: () -> Unit,
    onSave: (title: String, problemNumber: Int?, difficulty: String, topic: String, notes: String, timeSpentMinutes: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var problemNumberText by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Medium") }
    var topic by remember { mutableStateOf("Array") }
    var notes by remember { mutableStateOf("") }
    var timeSpentText by remember { mutableStateOf("20") }

    val difficulties = listOf("Easy", "Medium", "Hard")
    val topics = listOf(
        "Array", "String", "Two Pointers", "Sliding Window",
        "Stack", "Binary Search", "Trees", "Graph",
        "Dynamic Programming", "Hash Table", "Backtracking", "Greedy"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("log_leetcode_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log Solved Problem",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Difficulty Picker Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                difficulties.forEach { diff ->
                    val isSelected = difficulty == diff
                    val diffColor = when (diff) {
                        "Easy" -> Color(0xFF10B981)
                        "Medium" -> Color(0xFFF59E0B)
                        "Hard" -> Color(0xFFEF4444)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { difficulty = diff }
                            .testTag("diff_chip_$diff"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) diffColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) diffColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = diff,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) diffColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Problem Title & Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = problemNumberText,
                    onValueChange = { problemNumberText = it },
                    label = { Text("No.") },
                    placeholder = { Text("1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(90.dp)
                        .testTag("leetcode_num_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Problem Name") },
                    placeholder = { Text("e.g. Two Sum") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("leetcode_title_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // Topic Selector Chips
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Topic Category:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    topics.forEach { t ->
                        val isTopicSelected = topic == t
                        FilterChip(
                            selected = isTopicSelected,
                            onClick = { topic = t },
                            label = { Text(t, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Intuition / Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Key Intuition / Time Complexity") },
                placeholder = { Text("e.g. Hash map for complement. O(N) time and O(N) space.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp, max = 130.dp)
                    .testTag("leetcode_notes_input"),
                shape = RoundedCornerShape(16.dp)
            )

            // Time Spent Field
            OutlinedTextField(
                value = timeSpentText,
                onValueChange = { timeSpentText = it },
                label = { Text("Time Spent (Minutes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leetcode_time_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Submit Button
            Button(
                onClick = {
                    val pNum = problemNumberText.toIntOrNull()
                    val timeMin = timeSpentText.toIntOrNull() ?: 20
                    onSave(title, pNum, difficulty, topic, notes, timeMin)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_leetcode_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Solved Problem", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
