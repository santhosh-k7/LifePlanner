package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.ui.theme.*
import com.example.ui.viewmodel.LifePlannerViewModel
import com.example.ui.viewmodel.NoteFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadScreen(
    viewModel: LifePlannerViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.allNotes.collectAsState()
    val filter by viewModel.selectedNoteFilter.collectAsState()
    val searchQuery by viewModel.noteSearchQuery.collectAsState()
    val showAddEditSheet by viewModel.showAddEditNoteSheet.collectAsState()
    val editingNote by viewModel.editingNote.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val filteredNotes = remember(notes, filter, searchQuery) {
        notes.filter { note ->
            val matchesFilter = when (filter) {
                NoteFilter.ALL -> true
                NoteFilter.URGENT_IMPORTANT -> note.isUrgent && note.isImportant
                NoteFilter.URGENT -> note.isUrgent
                NoteFilter.IMPORTANT -> note.isImportant
                NoteFilter.PINNED -> note.isPinned
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                note.title.contains(searchQuery, ignoreCase = true) ||
                        note.content.contains(searchQuery, ignoreCase = true) ||
                        note.tag.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }
    }

    val urgentImportantCount = remember(notes) { notes.count { it.isUrgent && it.isImportant } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("notepad_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Hero Banner with Liquid Glass
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = Color(0xFF6366F1)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Urgent & Important",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.3).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Capture immediate thoughts, critical items, and urgent notes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (urgentImportantCount > 0) {
                                LiquidGlassPill(
                                    text = "$urgentImportantCount CRITICAL",
                                    accentColor = Color(0xFFEF4444)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Search within Notes
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.noteSearchQuery.value = it },
                            placeholder = { Text("Search notes...", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.noteSearchQuery.value = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("notepad_search_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1).copy(alpha = 0.6f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
                            )
                        )
                    }
                }
            }

            // Filter Chips Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteFilter.entries.forEach { f ->
                        val isSelected = filter == f
                        val chipColor = when (f) {
                            NoteFilter.ALL -> MaterialTheme.colorScheme.primary
                            NoteFilter.URGENT_IMPORTANT -> Color(0xFFEF4444)
                            NoteFilter.URGENT -> Color(0xFFF59E0B)
                            NoteFilter.IMPORTANT -> Color(0xFF8B5CF6)
                            NoteFilter.PINNED -> Color(0xFF3B82F6)
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { viewModel.selectedNoteFilter.value = f }
                                .testTag("filter_chip_${f.name}"),
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) chipColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) chipColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        ) {
                            Text(
                                text = f.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Notes List
            if (filteredNotes.isEmpty()) {
                item {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "📝", fontSize = 36.sp)
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No matching notes found" else "No notes in this category",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap the + button below to jot down an urgent or important note.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteCardItem(
                        note = note,
                        onPinToggle = { viewModel.togglePinNote(note.id) },
                        onEdit = { viewModel.openEditNote(note) },
                        onDelete = { viewModel.deleteNote(note) },
                        onCopy = {
                            val textToCopy = if (note.title.isNotBlank()) "${note.title}\n\n${note.content}" else note.content
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Floating Action Button for New Note
        FloatingActionButton(
            onClick = { viewModel.openAddNote() },
            containerColor = Color(0xFF6366F1),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("fab_add_note")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
                Text("New Note", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Add/Edit Note Modal Sheet
        if (showAddEditSheet) {
            AddEditNoteModal(
                editingNote = editingNote,
                onDismiss = { viewModel.showAddEditNoteSheet.value = false },
                onSave = { title, content, isUrgent, isImportant, isPinned, tag, colorHex ->
                    viewModel.saveNote(title, content, isUrgent, isImportant, isPinned, tag, colorHex)
                }
            )
        }
    }
}

@Composable
fun NoteCardItem(
    note: Note,
    onPinToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = remember(note.colorHex) { Color(note.colorHex) }
    val formattedDate = remember(note.updatedAt) {
        val sdf = SimpleDateFormat("dd-MM-yyyy · h:mm a", Locale.getDefault())
        sdf.format(Date(note.updatedAt))
    }

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("note_card_${note.id}"),
        accentColor = if (note.isUrgent) Color(0xFFEF4444) else accentColor,
        onClick = onEdit
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Badges & Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (note.isUrgent && note.isImportant) {
                        LiquidGlassPill(text = "🚨 URGENT & IMPORTANT", accentColor = Color(0xFFEF4444))
                    } else {
                        if (note.isUrgent) {
                            LiquidGlassPill(text = "⚡ URGENT", accentColor = Color(0xFFF59E0B))
                        }
                        if (note.isImportant) {
                            LiquidGlassPill(text = "⭐ IMPORTANT", accentColor = Color(0xFF8B5CF6))
                        }
                    }

                    if (note.tag.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "#${note.tag}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Actions: Pin, Copy, Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPinToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (note.isPinned) "Unpin" else "Pin",
                            tint = if (note.isPinned) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy Note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete Note",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Note Title
            if (note.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Note Content
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Updated $formattedDate",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteModal(
    editingNote: Note?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, isUrgent: Boolean, isImportant: Boolean, isPinned: Boolean, tag: String, colorHex: Long) -> Unit
) {
    var title by remember { mutableStateOf(editingNote?.title ?: "") }
    var content by remember { mutableStateOf(editingNote?.content ?: "") }
    var isUrgent by remember { mutableStateOf(editingNote?.isUrgent ?: false) }
    var isImportant by remember { mutableStateOf(editingNote?.isImportant ?: false) }
    var isPinned by remember { mutableStateOf(editingNote?.isPinned ?: false) }
    var tag by remember { mutableStateOf(editingNote?.tag ?: "General") }
    var selectedColorHex by remember { mutableStateOf(editingNote?.colorHex ?: 0xFF6366F1) }

    val presetColors = listOf(
        0xFF6366F1 to "Indigo",
        0xFFEF4444 to "Rose",
        0xFFF59E0B to "Amber",
        0xFF10B981 to "Emerald",
        0xFF38BDF8 to "Cyan",
        0xFFA855F7 to "Purple"
    )

    val presetTags = listOf("General", "Work", "Urgent", "Idea", "Checklist", "Personal")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("add_edit_note_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Modal Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingNote == null) "New Note" else "Edit Note",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Eisenhower Urgency & Importance Switches
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = if (isUrgent) Color(0xFFEF4444) else Color(0xFF6366F1)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🚨", fontSize = 18.sp)
                            Column {
                                Text("Urgent Note", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Needs prompt, immediate attention", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isUrgent,
                            onCheckedChange = { isUrgent = it },
                            modifier = Modifier.testTag("switch_note_urgent")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("⭐", fontSize = 18.sp)
                            Column {
                                Text("Important Note", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("High long-term value or critical reference", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isImportant,
                            onCheckedChange = { isImportant = it },
                            modifier = Modifier.testTag("switch_note_important")
                        )
                    }
                }
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Note Title") },
                placeholder = { Text("e.g. Critical meeting follow-up") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Content Field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Note Content / Checklist") },
                placeholder = { Text("Type details, reminders, code notes, or action items...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp)
                    .testTag("note_content_input"),
                shape = RoundedCornerShape(16.dp)
            )

            // Tags row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetTags.forEach { t ->
                    val isTagSelected = tag.equals(t, ignoreCase = true)
                    FilterChip(
                        selected = isTagSelected,
                        onClick = { tag = t },
                        label = { Text(t, fontSize = 12.sp) }
                    )
                }
            }

            // Color accents row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Color:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                presetColors.forEach { (colorLong, _) ->
                    val isColorSelected = selectedColorHex == colorLong
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(colorLong))
                            .clickable { selectedColorHex = colorLong }
                            .then(
                                if (isColorSelected) {
                                    Modifier.background(
                                        Color.White.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isColorSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    onSave(title, content, isUrgent, isImportant, isPinned, tag, selectedColorHex)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_note_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (editingNote == null) "Create Note" else "Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
