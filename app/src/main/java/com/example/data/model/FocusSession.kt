package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val durationSeconds: Int,
    val sessionType: String = "POMODORO_FOCUS", // "POMODORO_FOCUS", "SHORT_BREAK", "LONG_BREAK", "STOPWATCH"
    val completedAt: Long = System.currentTimeMillis()
)
