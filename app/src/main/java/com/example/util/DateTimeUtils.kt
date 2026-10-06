package com.example.util

import com.example.data.model.HabitLog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateTimeUtils {

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d")
    private val FULL_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d")

    fun today(): LocalDate = LocalDate.now()

    fun todayStr(): String = today().format(DATE_FORMATTER)

    fun getGreeting(): String {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Night owl mode"
        }
    }

    fun formatHeaderDate(date: LocalDate = today()): String {
        return date.format(FULL_DISPLAY_FORMATTER)
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr, DATE_FORMATTER)
            val today = today()
            val diff = ChronoUnit.DAYS.between(today, date)
            when (diff) {
                0L -> "Today, ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
                1L -> "Tomorrow, ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
                -1L -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
                in 2..6 -> date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatShortDay(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr, DATE_FORMATTER)
            date.format(DateTimeFormatter.ofPattern("EEE"))
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatShortDayNumber(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr, DATE_FORMATTER)
            date.dayOfMonth.toString()
        } catch (e: Exception) {
            ""
        }
    }

    fun formatTime12H(timeStr: String?): String {
        if (timeStr.isNullOrBlank()) return ""
        return try {
            val parts = timeStr.split(":")
            val hour = parts[0].toInt()
            val min = parts[1].toInt()
            val amPm = if (hour >= 12) "PM" else "AM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            String.format(Locale.US, "%d:%02d %s", displayHour, min, amPm)
        } catch (e: Exception) {
            timeStr
        }
    }

    fun formatSecondsToMmSs(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun calculateHabitStreaks(
        logs: List<HabitLog>,
        graceDaysPerWeek: Int = 1
    ): Pair<Int, Int> { // Pair(currentStreak, bestStreak)
        if (logs.isEmpty()) return Pair(0, 0)

        val logDates = logs.filter { it.isCompleted }.map { it.date }.toSet()
        if (logDates.isEmpty()) return Pair(0, 0)

        val today = today()
        var currentStreak = 0
        var bestStreak = 0
        var streak = 0
        var graceUsedThisWeek = 0
        var currentWeekNumber = -1

        // Check recent days backwards for current streak
        var checkDate = today
        // If today is not logged yet, start checking from yesterday so active streak is not broken
        if (!logDates.contains(today.toString())) {
            checkDate = today.minusDays(1)
        }

        var tempDate = checkDate
        var missedStreakDaysInARow = 0

        while (true) {
            val dStr = tempDate.toString()
            if (logDates.contains(dStr)) {
                currentStreak++
                missedStreakDaysInARow = 0
            } else {
                // Check if grace day covers it
                if (graceDaysPerWeek > 0 && missedStreakDaysInARow == 0) {
                    // One grace day permitted
                    missedStreakDaysInARow++
                } else {
                    break
                }
            }
            tempDate = tempDate.minusDays(1)
            if (ChronoUnit.DAYS.between(tempDate, today) > 365) break
        }

        // Calculate all-time best streak
        val sortedDates = logDates.map { LocalDate.parse(it, DATE_FORMATTER) }.sorted()
        streak = 0
        var lastDate: LocalDate? = null

        for (date in sortedDates) {
            if (lastDate == null) {
                streak = 1
            } else {
                val gap = ChronoUnit.DAYS.between(lastDate, date)
                if (gap == 1L) {
                    streak++
                } else if (gap == 2L && graceDaysPerWeek > 0) {
                    // Grace covered
                    streak += 2
                } else {
                    streak = 1
                }
            }
            if (streak > bestStreak) bestStreak = streak
            lastDate = date
        }

        bestStreak = maxOf(bestStreak, currentStreak)
        return Pair(currentStreak, bestStreak)
    }

    fun getUpcomingDays(count: Int = 14): List<LocalDate> {
        val list = mutableListOf<LocalDate>()
        val today = today()
        for (i in 0 until count) {
            list.add(today.plusDays(i.toLong()))
        }
        return list
    }
}
