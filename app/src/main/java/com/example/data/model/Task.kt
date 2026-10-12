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
    val medium: String = "", // Legacy field (removed from UI)
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val repeatRule: RepeatRule = RepeatRule.NONE,
    val reminderOffsetMin: Int? = null, // Legacy single offset (e.g. 0, 10, 30)
    val reminderOffsets: String = "", // Comma-separated offsets in minutes, e.g. "0,15" (at time of event + 15m before)
    val snoozedUntilDate: String? = null,
    val orderIndex: Int = 0,
    val focusMinutesSpent: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Returns list of notification reminder minute offsets.
     * 0 = at time of event, 15 = 15 minutes before, 60 = 1 hour before, etc.
     */
    fun getReminderOffsetList(): List<Int> {
        if (reminderOffsets.isNotBlank()) {
            return reminderOffsets.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .distinct()
                .sorted()
        }
        return reminderOffsetMin?.let { listOf(it) } ?: emptyList()
    }
}
