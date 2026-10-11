package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Priority(val label: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

enum class RepeatRule(val label: String) {
    NONE("Does not repeat"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

@Entity(
    tableName = "tasks",
    indices = [
        Index("dueDate"),
        Index("isCompleted"),
        Index("categoryId")
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueDate: String, // Format: dd-MM-yyyy e.g. "11-10-2026"
    val dueTime: String? = null, // Format: HH:mm e.g. "14:30"
    val durationMin: Int = 0, // e.g. 15, 30, 45, 60 min for time blocking
    val priority: Priority = Priority.MEDIUM,
    val categoryId: String = "personal", // "work", "personal", "health", "learning", "coding", "other"
    val medium: String = "", // e.g. "Online", "In-Person", "Phone Call", "Meet / Zoom", "Desk Work", "Coding"
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val repeatRule: RepeatRule = RepeatRule.NONE,
    val reminderOffsetMin: Int? = null, // e.g. 0 (at time), 10, 30, 60
    val snoozedUntilDate: String? = null,
    val orderIndex: Int = 0,
    val focusMinutesSpent: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
