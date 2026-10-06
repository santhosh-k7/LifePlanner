package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.HabitDetailCard
import com.example.ui.components.RoutineCard
import com.example.ui.viewmodel.LifePlannerViewModel

@Composable
fun RoutinesHabitsScreen(
    viewModel: LifePlannerViewModel,
    modifier: Modifier = Modifier
) {
    val subTab by viewModel.routinesHabitsSubTab.collectAsState() // 0: Routines, 1: Habits
    val routines by viewModel.allRoutines.collectAsState()
    val habits by viewModel.allHabits.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showAddRoutineDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("routines_habits_screen")
    ) {
        // Tab Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Routines & Habits",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = subTab == 0,
                    onClick = { viewModel.routinesHabitsSubTab.value = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {}
                ) {
                    Icon(Icons.Outlined.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Routines", fontSize = 12.sp)
                }

                SegmentedButton(
                    selected = subTab == 1,
                    onClick = { viewModel.routinesHabitsSubTab.value = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {}
                ) {
                    Icon(Icons.Outlined.Spa, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Habits", fontSize = 12.sp)
                }
            }
        }

        if (subTab == 0) {
            // ROUTINES SUB-TAB
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("routines_list_view"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Routines (${routines.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FilledTonalButton(
                            onClick = { showAddRoutineDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Routine", fontSize = 13.sp)
                        }
                    }
                }

                items(routines, key = { it.routine.id }) { routineItem ->
                    RoutineCard(
                        routineWithSteps = routineItem,
                        onRunRoutine = { viewModel.startRoutineRunner(routineItem) },
                        onToggleStep = { stepId, done -> viewModel.markRoutineStepDone(stepId, done) },
                        onDelete = { viewModel.deleteRoutine(routineItem.routine) }
                    )
                }
            }
        } else {
            // HABITS SUB-TAB
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("habits_list_view"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Habits & Consistency",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "1 grace day per week protection",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        FilledTonalButton(
                            onClick = { showAddHabitDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Habit", fontSize = 13.sp)
                        }
                    }
                }

                items(habits, key = { it.habit.id }) { habitItem ->
                    HabitDetailCard(
                        habitWithLogs = habitItem,
                        onToggleDate = { dateStr -> viewModel.toggleHabitOnDate(habitItem.habit.id, dateStr) },
                        onDelete = { viewModel.deleteHabit(habitItem.habit) }
                    )
                }
            }
        }
    }

    // Add Habit Dialog
    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onAdd = { name, icon, targetDays, graceDays, colorHex ->
                viewModel.addHabit(name, icon, targetDays, graceDays, colorHex)
                showAddHabitDialog = false
            }
        )
    }

    // Add Routine Dialog
    if (showAddRoutineDialog) {
        AddRoutineDialog(
            onDismiss = { showAddRoutineDialog = false },
            onAdd = { name, icon, timeOfDay, startTime, steps ->
                viewModel.addRoutine(name, icon, timeOfDay, startTime, steps)
                showAddRoutineDialog = false
            }
        )
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, Int, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🎯") }
    var targetDays by remember { mutableIntStateOf(7) }
    var graceDays by remember { mutableIntStateOf(1) }

    val emojis = listOf("🎯", "💧", "🚶", "📖", "✨", "🧘", "🥗", "🏋️", "💡", "💤")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "New Habit",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name (e.g. Drink 2L Water)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Emoji Picker
                Column {
                    Text("Select Icon", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        emojis.forEach { e ->
                            FilterChip(
                                selected = emoji == e,
                                onClick = { emoji = e },
                                label = { Text(e, fontSize = 16.sp) }
                            )
                        }
                    }
                }

                // Target Frequency
                Column {
                    Text("Target Frequency: $targetDays days per week", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        listOf(3, 4, 5, 6, 7).forEach { days ->
                            FilterChip(
                                selected = targetDays == days,
                                onClick = { targetDays = days },
                                label = { Text("${days}d") }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAdd(name.trim(), emoji, targetDays, graceDays, 0xFF0D9488)
                            }
                        }
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, List<Pair<String, Int>>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("☀️") }
    var timeOfDay by remember { mutableStateOf("Morning") }
    var startTime by remember { mutableStateOf("08:00") }
    var stepsText by remember { mutableStateOf("Step 1 (5m)\nStep 2 (10m)\nStep 3 (15m)") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "New Routine",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name (e.g. Deep Work Reset)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = timeOfDay,
                        onValueChange = { timeOfDay = it },
                        label = { Text("Time of Day") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = stepsText,
                    onValueChange = { stepsText = it },
                    label = { Text("Steps (one per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val steps = stepsText.lines()
                                    .filter { it.isNotBlank() }
                                    .map { Pair(it.trim(), 5) }
                                onAdd(name.trim(), emoji, timeOfDay, startTime, steps)
                            }
                        }
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
