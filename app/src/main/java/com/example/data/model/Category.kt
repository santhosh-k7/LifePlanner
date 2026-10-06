package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String, // "work", "personal", "health", "learning", "others"
    val name: String,
    val colorHex: Long,
    val iconName: String
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category("work", "Work", 0xFF3B82F6, "Briefcase"),
            Category("personal", "Personal", 0xFF8B5CF6, "User"),
            Category("health", "Health", 0xFF10B981, "Heart"),
            Category("learning", "Learning", 0xFFF59E0B, "BookOpen"),
            Category("others", "Others", 0xFF64748B, "MoreHoriz")
        )

        fun getColor(categoryId: String): Color {
            return when (categoryId.lowercase()) {
                "work" -> Color(0xFF3B82F6)
                "personal" -> Color(0xFF8B5CF6)
                "health" -> Color(0xFF10B981)
                "learning" -> Color(0xFFF59E0B)
                "other", "others" -> Color(0xFF64748B)
                else -> Color(0xFF64748B)
            }
        }

        fun getDisplayName(categoryId: String): String {
            return when (categoryId.lowercase()) {
                "work" -> "Work"
                "personal" -> "Personal"
                "health" -> "Health"
                "learning" -> "Learning"
                "other", "others" -> "Others"
                else -> categoryId.replaceFirstChar { it.uppercase() }
            }
        }
    }
}
