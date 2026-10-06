package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Priority
import com.example.ui.theme.*
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.util.DateTimeUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    viewModel: LifePlannerViewModel,
    modifier: Modifier = Modifier
) {
    val input by viewModel.quickAddInput.collectAsState()
    val draft by viewModel.quickAddDraft.collectAsState()
    val overrideDate by viewModel.quickAddOverrideDate.collectAsState()
    val overridePriority by viewModel.quickAddOverridePriority.collectAsState()
    val overrideCategory by viewModel.quickAddOverrideCategory.collectAsState()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.showQuickAdd.value = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier.testTag("quick_add_bottom_sheet"),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Capture",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "e.g. Call gym tomorrow 6pm !high #health",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Natural Language Single Input Field
            OutlinedTextField(
                value = input,
                onValueChange = { viewModel.updateQuickAddInput(it) },
                placeholder = { Text("What needs doing? Try typing time, date, !priority, #category...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("quick_add_input"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.submitQuickAdd(stayOpen = false) }
                ),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Live Parsed Detection Badges
            val effectiveDate = overrideDate ?: draft.dueDate
            val effectivePriority = overridePriority ?: draft.priority
            val effectiveCategory = overrideCategory ?: draft.categoryId

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Detected Date Chip
                AssistChip(
                    onClick = {
                        // Quick toggle: Today -> Tomorrow -> In 3 Days
                        val current = LocalDate.parse(effectiveDate)
                        val next = if (current == LocalDate.now()) {
                            LocalDate.now().plusDays(1)
                        } else {
                            LocalDate.now()
                        }
                        viewModel.quickAddOverrideDate.value = next.toString()
                    },
                    label = {
                        Text(
                            text = DateTimeUtils.formatDisplayDate(effectiveDate),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        labelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Detected Time Chip
                if (!draft.dueTime.isNullOrBlank()) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = DateTimeUtils.formatTime12H(draft.dueTime),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                // Detected Priority Chip (tap to cycle)
                AssistChip(
                    onClick = {
                        val nextP = when (effectivePriority) {
                            Priority.LOW -> Priority.MEDIUM
                            Priority.MEDIUM -> Priority.HIGH
                            Priority.HIGH -> Priority.LOW
                        }
                        viewModel.quickAddOverridePriority.value = nextP
                    },
                    label = {
                        Text(
                            text = effectivePriority.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = when (effectivePriority) {
                                Priority.HIGH -> PriorityHigh
                                Priority.MEDIUM -> PriorityMedium
                                Priority.LOW -> PriorityLow
                            }
                        )
                    }
                )

                // Detected Category Chip (tap to cycle)
                val catColor = Category.getColor(effectiveCategory)
                AssistChip(
                    onClick = {
                        val allCats = listOf("personal", "work", "health", "learning")
                        val nextIdx = (allCats.indexOf(effectiveCategory) + 1) % allCats.size
                        viewModel.quickAddOverrideCategory.value = allCats[nextIdx]
                    },
                    label = {
                        Text(
                            text = effectiveCategory.replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            color = catColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Label, contentDescription = null, modifier = Modifier.size(14.dp), tint = catColor)
                    }
                )

                // Detected Repeat Chip
                if (draft.repeatRule.name != "NONE") {
                    AssistChip(
                        onClick = {},
                        label = { Text(draft.repeatRule.label, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Repeat, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.submitQuickAdd(stayOpen = true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_add_stay_open_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add Another", fontSize = 14.sp)
                }

                Button(
                    onClick = { viewModel.submitQuickAdd(stayOpen = false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_add_submit_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Task", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
