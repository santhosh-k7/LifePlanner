package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconEmoji: String = "💧",
    val targetDaysPerWeek: Int = 7, // e.g. 7 for daily, 3 for 3x/week
    val graceDaysPerWeek: Int = 1, // forgiving streak system
    val colorHex: Long = 0xFF0D9488,
    val createdAt: Long = System.currentTimeMillis()
)
