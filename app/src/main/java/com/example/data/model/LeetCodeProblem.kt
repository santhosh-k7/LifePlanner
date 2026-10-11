package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leetcode_problems")
data class LeetCodeProblem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val problemNumber: Int? = null,
    val title: String,
    val difficulty: String = "Medium", // "Easy", "Medium", "Hard"
    val topic: String = "Array", // "Array", "DP", "Trees", "Graph", "String", "Two Pointers", etc.
    val dateSolved: String, // Format: YYYY-MM-DD
    val notes: String = "", // Key intuition / Time complexity
    val timeSpentMinutes: Int = 20,
    val solvedAt: Long = System.currentTimeMillis()
)
