package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.TaskWithSubtasks
import com.example.util.DateTimeUtils

@Composable
fun TimeBlockingTimeline(
    timedTasks: List<TaskWithSubtasks>,
    onTaskClick: (TaskWithSubtasks) -> Unit,
    onStartFocus: (TaskWithSubtasks) -> Unit,
    modifier: Modifier = Modifier
) {
    if (timedTasks.isEmpty()) return

    // Sort by dueTime
    val sorted = timedTasks.sortedBy { it.task.dueTime ?: "99:99" }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sorted.forEachIndexed { index, item ->
            val task = item.task
            val categoryColor = Category.getColor(task.categoryId)
            val prevTask = sorted.getOrNull(index - 1)?.task

            // Check overlap if previous task had end time that exceeds this start time
            val isOverlapping = if (prevTask != null && !prevTask.dueTime.isNullOrBlank() && !task.dueTime.isNullOrBlank()) {
                val prevParts = prevTask.dueTime.split(":")
                val currParts = task.dueTime.split(":")
                val prevStartMin = (prevParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (prevParts.getOrNull(1)?.toIntOrNull() ?: 0)
                val prevEndMin = prevStartMin + (if (prevTask.durationMin > 0) prevTask.durationMin else 30)
                val currStartMin = (currParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (currParts.getOrNull(1)?.toIntOrNull() ?: 0)
                currStartMin < prevEndMin
            } else false

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timeline_item_${task.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time label on left
                Column(
                    modifier = Modifier.widthIn(min = 52.dp, max = 68.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = DateTimeUtils.formatTime12H(task.dueTime),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = "${if (task.durationMin > 0) task.durationMin else 30} min",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Timeline vertical indicator line & node
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Colored time-block card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTaskClick(item) },
                    colors = CardDefaults.cardColors(
                        containerColor = categoryColor.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored accent stripe
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(categoryColor)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = Category.getDisplayName(task.categoryId),
                                    fontSize = 11.sp,
                                    color = categoryColor,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (isOverlapping) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Schedule overlap",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Overlap",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Focus quick button
                        IconButton(
                            onClick = { onStartFocus(item) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Focus on time block",
                                tint = categoryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
