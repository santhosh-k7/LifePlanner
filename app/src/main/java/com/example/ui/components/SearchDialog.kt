package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    viewModel: LifePlannerViewModel,
    onDismiss: () -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    var showOnlyCompleted by remember { mutableStateOf(false) }

    val filteredTasks = remember(query, showOnlyCompleted, allTasks) {
        val q = query.trim().lowercase()
        allTasks.filter { item ->
            val task = item.task
            val matchesFilter = if (showOnlyCompleted) task.isCompleted else true
            val matchesQuery = if (q.isBlank()) true else {
                task.title.lowercase().contains(q) ||
                        task.notes.lowercase().contains(q) ||
                        task.categoryId.lowercase().contains(q) ||
                        item.subtasks.any { it.title.lowercase().contains(q) }
            }
            matchesFilter && matchesQuery
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("search_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Search Input & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search tasks, notes, subtasks...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close search")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filters row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !showOnlyCompleted,
                        onClick = { showOnlyCompleted = false },
                        label = { Text("All Tasks") }
                    )
                    FilterChip(
                        selected = showOnlyCompleted,
                        onClick = { showOnlyCompleted = true },
                        label = { Text("Completed Only") }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${filteredTasks.size} found",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Results list
                if (filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (query.isBlank()) "Type keywords to search" else "No matching tasks found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredTasks, key = { it.task.id }) { item ->
                            TaskRowItem(
                                taskWithSubtasks = item,
                                onToggleComplete = { viewModel.toggleTaskComplete(item.task) },
                                onClick = {
                                    onDismiss()
                                    viewModel.selectedTaskForDetail.value = item
                                },
                                onStartFocus = {
                                    onDismiss()
                                    viewModel.openFocusMode(item.task)
                                },
                                onReschedule = {
                                    onDismiss()
                                    viewModel.taskToReschedule.value = item.task
                                },
                                onDelete = { viewModel.deleteTask(item.task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
