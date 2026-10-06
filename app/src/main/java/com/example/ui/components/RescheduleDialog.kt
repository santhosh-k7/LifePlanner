package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.util.DateTimeUtils
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleDialog(
    task: Task,
    viewModel: LifePlannerViewModel,
    onDismiss: () -> Unit
) {
    val today = LocalDate.now()
    var customDateInput by remember { mutableStateOf(task.dueDate) }
    var showCustomInput by remember { mutableStateOf(false) }

    // Calculate presets
    val tomorrow = today.plusDays(1)
    val nextMonday = run {
        var d = today.plusDays(1)
        while (d.dayOfWeek != DayOfWeek.MONDAY) {
            d = d.plusDays(1)
        }
        d
    }
    val thisWeekend = run {
        var d = today
        while (d.dayOfWeek != DayOfWeek.SATURDAY) {
            d = d.plusDays(1)
        }
        d
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("reschedule_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Reschedule \"${task.title}\"",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Presets List
            ReschedulePresetItem(
                title = "Today",
                subtitle = DateTimeUtils.formatDisplayDate(today.toString()),
                icon = Icons.Outlined.Today,
                onClick = { viewModel.rescheduleTask(task.id, today.toString()) }
            )

            ReschedulePresetItem(
                title = "Tomorrow",
                subtitle = DateTimeUtils.formatDisplayDate(tomorrow.toString()),
                icon = Icons.Outlined.WbSunny,
                onClick = { viewModel.rescheduleTask(task.id, tomorrow.toString()) }
            )

            ReschedulePresetItem(
                title = "This Weekend",
                subtitle = DateTimeUtils.formatDisplayDate(thisWeekend.toString()),
                icon = Icons.Outlined.Weekend,
                onClick = { viewModel.rescheduleTask(task.id, thisWeekend.toString()) }
            )

            ReschedulePresetItem(
                title = "Next Week",
                subtitle = DateTimeUtils.formatDisplayDate(nextMonday.toString()),
                icon = Icons.Outlined.NextWeek,
                onClick = { viewModel.rescheduleTask(task.id, nextMonday.toString()) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (!showCustomInput) {
                TextButton(
                    onClick = { showCustomInput = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick Custom Date")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customDateInput,
                        onValueChange = { customDateInput = it },
                        label = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customDateInput.isNotBlank()) {
                                viewModel.rescheduleTask(task.id, customDateInput.trim())
                            }
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReschedulePresetItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
