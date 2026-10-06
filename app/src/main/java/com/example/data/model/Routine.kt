package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconEmoji: String = "☀️",
    val timeOfDay: String = "Morning", // "Morning", "Afternoon", "Evening", "Night", "Workout"
    val startTime: String = "08:00",
    val daysOfWeek: String = "1,2,3,4,5,6,7", // Monday(1) to Sunday(7)
    val isTemplate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
