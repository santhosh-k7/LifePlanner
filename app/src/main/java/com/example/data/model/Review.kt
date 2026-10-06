package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reviewType: String, // "DAILY" or "WEEKLY"
    val date: String, // YYYY-MM-DD
    val completedTasksCount: Int = 0,
    val totalTasksCount: Int = 0,
    val completionRatePercent: Int = 0,
    val habitConsistencyPercent: Int = 0,
    val focusMinutesTotal: Int = 0,
    val reflectionWell: String = "",
    val reflectionImprove: String = "",
    val nextWeekPriorities: String = "", // Stored as pipe or comma-separated items
    val createdAt: Long = System.currentTimeMillis()
)
