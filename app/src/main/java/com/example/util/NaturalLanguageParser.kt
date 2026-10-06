package com.example.util

import com.example.data.model.Priority
import com.example.data.model.RepeatRule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

data class ParsedTaskDraft(
    val rawInput: String,
    val title: String,
    val dueDate: String, // YYYY-MM-DD
    val dueTime: String? = null, // HH:mm
    val priority: Priority = Priority.MEDIUM,
    val categoryId: String = "personal",
    val repeatRule: RepeatRule = RepeatRule.NONE,
    val durationMin: Int = 0
)

object NaturalLanguageParser {

    private val TIME_12H_REGEX = Pattern.compile("(?i)\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b")
    private val TIME_24H_REGEX = Pattern.compile("\\b([01]?\\d|2[0-3]):([0-5]\\d)\\b")
    private val PRIORITY_REGEX = Pattern.compile("(?i)!(high|med|medium|low)")
    private val CATEGORY_REGEX = Pattern.compile("(?i)#([a-zA-Z0-9_-]+)")
    private val IN_DAYS_REGEX = Pattern.compile("(?i)\\bin\\s+(\\d+)\\s+days?\\b")
    private val REPEAT_REGEX = Pattern.compile("(?i)\\b(every\\s+(day|daily|monday|tuesday|wednesday|thursday|friday|saturday|sunday|week|month))\\b")
    private val DURATION_REGEX = Pattern.compile("(?i)\\b(\\d+)\\s*(m|min|mins|minutes|h|hr|hrs|hours)\\b")

    fun parse(input: String, baseDate: LocalDate = LocalDate.now()): ParsedTaskDraft {
        if (input.isBlank()) {
            return ParsedTaskDraft(
                rawInput = "",
                title = "",
                dueDate = baseDate.toString(),
                dueTime = null,
                priority = Priority.MEDIUM,
                categoryId = "personal",
                repeatRule = RepeatRule.NONE
            )
        }

        var workingText = input
        var detectedDate = baseDate
        var detectedTime: String? = null
        var detectedPriority = Priority.MEDIUM
        var detectedCategory = "personal"
        var detectedRepeat = RepeatRule.NONE
        var detectedDuration = 0

        // 1. Extract Priority (!high, !med, !low)
        val priorityMatcher = PRIORITY_REGEX.matcher(workingText)
        if (priorityMatcher.find()) {
            val pStr = priorityMatcher.group(1).lowercase(Locale.ROOT)
            detectedPriority = when {
                pStr.startsWith("high") -> Priority.HIGH
                pStr.startsWith("low") -> Priority.LOW
                else -> Priority.MEDIUM
            }
            workingText = priorityMatcher.replaceFirst(" ")
        }

        // 2. Extract Category (#work, #personal, #health, #learning, etc.)
        val categoryMatcher = CATEGORY_REGEX.matcher(workingText)
        if (categoryMatcher.find()) {
            val catStr = categoryMatcher.group(1).lowercase(Locale.ROOT)
            detectedCategory = when (catStr) {
                "work", "job" -> "work"
                "personal" -> "personal"
                "health", "fitness", "gym" -> "health"
                "learning", "study", "code", "book" -> "learning"
                "other", "others", "misc", "general" -> "other"
                else -> "other"
            }
            workingText = categoryMatcher.replaceFirst(" ")
        }

        // 3. Extract Repeat ("every day", "every monday")
        val repeatMatcher = REPEAT_REGEX.matcher(workingText)
        if (repeatMatcher.find()) {
            val rStr = repeatMatcher.group(2).lowercase(Locale.ROOT)
            detectedRepeat = when {
                rStr.contains("day") || rStr.contains("daily") -> RepeatRule.DAILY
                rStr.contains("month") -> RepeatRule.MONTHLY
                else -> RepeatRule.WEEKLY
            }
            workingText = repeatMatcher.replaceFirst(" ")
        }

        // 4. Extract "in X days"
        val inDaysMatcher = IN_DAYS_REGEX.matcher(workingText)
        if (inDaysMatcher.find()) {
            val daysCount = inDaysMatcher.group(1).toLongOrNull() ?: 0L
            detectedDate = baseDate.plusDays(daysCount)
            workingText = inDaysMatcher.replaceFirst(" ")
        }

        // 5. Extract "tomorrow" or "today" or weekdays ("friday", "next monday")
        val lowerWorking = workingText.lowercase(Locale.ROOT)
        when {
            lowerWorking.contains(Regex("\\btomorrow\\b")) -> {
                detectedDate = baseDate.plusDays(1)
                workingText = workingText.replace(Regex("(?i)\\btomorrow\\b"), " ")
            }
            lowerWorking.contains(Regex("\\btoday\\b")) -> {
                detectedDate = baseDate
                workingText = workingText.replace(Regex("(?i)\\btoday\\b"), " ")
            }
            lowerWorking.contains(Regex("\\btonight\\b")) -> {
                detectedDate = baseDate
                detectedTime = "20:00"
                workingText = workingText.replace(Regex("(?i)\\btonight\\b"), " ")
            }
            else -> {
                // Check day of week names
                val dayMap = mapOf(
                    "monday" to DayOfWeek.MONDAY,
                    "mon" to DayOfWeek.MONDAY,
                    "tuesday" to DayOfWeek.TUESDAY,
                    "tue" to DayOfWeek.TUESDAY,
                    "wednesday" to DayOfWeek.WEDNESDAY,
                    "wed" to DayOfWeek.WEDNESDAY,
                    "thursday" to DayOfWeek.THURSDAY,
                    "thu" to DayOfWeek.THURSDAY,
                    "friday" to DayOfWeek.FRIDAY,
                    "fri" to DayOfWeek.FRIDAY,
                    "saturday" to DayOfWeek.SATURDAY,
                    "sat" to DayOfWeek.SATURDAY,
                    "sunday" to DayOfWeek.SUNDAY,
                    "sun" to DayOfWeek.SUNDAY
                )

                for ((name, targetDow) in dayMap) {
                    val pattern = Pattern.compile("(?i)\\b(?:next\\s+)?$name\\b")
                    val m = pattern.matcher(workingText)
                    if (m.find()) {
                        var target = baseDate.plusDays(1)
                        while (target.dayOfWeek != targetDow) {
                            target = target.plusDays(1)
                        }
                        detectedDate = target
                        workingText = m.replaceFirst(" ")
                        break
                    }
                }
            }
        }

        // 6. Extract 12-hour Time ("6pm", "9:30am")
        val time12Matcher = TIME_12H_REGEX.matcher(workingText)
        if (time12Matcher.find()) {
            val hourStr = time12Matcher.group(1)
            val minStr = time12Matcher.group(2) ?: "00"
            val amPm = time12Matcher.group(3).lowercase(Locale.ROOT)
            var hour = hourStr.toIntOrNull() ?: 12
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            val min = minStr.toIntOrNull() ?: 0
            detectedTime = String.format(Locale.US, "%02d:%02d", hour, min)
            workingText = time12Matcher.replaceFirst(" ")
        } else {
            // Check 24-hour Time ("15:30")
            val time24Matcher = TIME_24H_REGEX.matcher(workingText)
            if (time24Matcher.find()) {
                val hour = time24Matcher.group(1).toIntOrNull() ?: 0
                val min = time24Matcher.group(2).toIntOrNull() ?: 0
                detectedTime = String.format(Locale.US, "%02d:%02d", hour, min)
                workingText = time24Matcher.replaceFirst(" ")
            }
        }

        // 7. Extract Duration ("30m", "1h", "45 mins")
        val durationMatcher = DURATION_REGEX.matcher(workingText)
        if (durationMatcher.find()) {
            val amount = durationMatcher.group(1).toIntOrNull() ?: 0
            val unit = durationMatcher.group(2).lowercase(Locale.ROOT)
            detectedDuration = if (unit.startsWith("h")) amount * 60 else amount
            workingText = durationMatcher.replaceFirst(" ")
        }

        // Clean title
        val cleanedTitle = workingText
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { input.trim() }

        return ParsedTaskDraft(
            rawInput = input,
            title = cleanedTitle,
            dueDate = detectedDate.toString(),
            dueTime = detectedTime,
            priority = detectedPriority,
            categoryId = detectedCategory,
            repeatRule = detectedRepeat,
            durationMin = detectedDuration
        )
    }
}
