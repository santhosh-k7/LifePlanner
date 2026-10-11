package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val isUrgent: Boolean = false,
    val isImportant: Boolean = false,
    val isPinned: Boolean = false,
    val tag: String = "General",
    val colorHex: Long = 0xFF6366F1, // Default Indigo/Glass accent
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val isEisenhowerUrgentImportant: Boolean
        get() = isUrgent && isImportant
}
