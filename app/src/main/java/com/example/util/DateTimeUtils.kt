package com.example.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateTimeUtils {

    val DATE_FORMATTER_DD_MM_YYYY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val DATE_FORMATTER_ISO: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun today(): LocalDate = LocalDate.now()

    /**
     * Standard date string in requested format: dd-MM-yyyy (e.g. "11-10-2026")
     */
    fun todayStr(): String = today().format(DATE_FORMATTER_DD_MM_YYYY)

    fun formatDate(date: LocalDate): String = date.format(DATE_FORMATTER_DD_MM_YYYY)

    /**
     * Safely parse a date string supporting both dd-MM-yyyy and yyyy-MM-dd.
     */
    fun parseDate(dateStr: String?): LocalDate {
        if (dateStr.isNullOrBlank()) return today()
        return try {
            val trimmed = dateStr.trim()
            if (trimmed.contains("-")) {
                val parts = trimmed.split("-")
                if (parts[0].length == 4) {
                    // yyyy-MM-dd
                    LocalDate.parse(trimmed, DATE_FORMATTER_ISO)
                } else {
                    // dd-MM-yyyy
                    LocalDate.parse(trimmed, DATE_FORMATTER_DD_MM_YYYY)
                }
            } else {
                today()
            }
        } catch (_: Exception) {
            today()
        }
    }

    /**
     * Format any date string to standard dd-MM-yyyy.
     */
    fun toDdMmYyyy(dateStr: String?): String {
        return formatDate(parseDate(dateStr))
    }

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
        val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEEE"))
        val formattedDate = date.format(DATE_FORMATTER_DD_MM_YYYY)
        return "$dayOfWeek, $formattedDate"
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = parseDate(dateStr)
            val today = today()
            val diff = ChronoUnit.DAYS.between(today, date)
            val formatted = date.format(DATE_FORMATTER_DD_MM_YYYY)
            val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEE"))
            when (diff) {
                0L -> "Today, $formatted"
                1L -> "Tomorrow, $formatted"
                -1L -> "Yesterday, $formatted"
                else -> "$dayOfWeek, $formatted"
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatShortDay(dateStr: String): String {
        return try {
            val date = parseDate(dateStr)
            date.format(DateTimeFormatter.ofPattern("EEE"))
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatShortDayNumber(dateStr: String): String {
        return try {
            val date = parseDate(dateStr)
            date.dayOfMonth.toString()
        } catch (e: Exception) {
            ""
        }
    }

    fun formatTime12H(timeStr: String?): String {
        if (timeStr.isNullOrBlank()) return ""
        return try {
            val parts = timeStr.trim().split(":")
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

    fun getUpcomingDays(count: Int = 14): List<LocalDate> {
        val list = mutableListOf<LocalDate>()
        val today = today()
        for (i in 0 until count) {
            list.add(today.plusDays(i.toLong()))
        }
        return list
    }

    /**
     * Returns all days for a given year and month.
     */
    fun getDaysInMonth(year: Int, month: Int): List<LocalDate> {
        val ym = YearMonth.of(year, month)
        val length = ym.lengthOfMonth()
        return (1..length).map { day -> LocalDate.of(year, month, day) }
    }
}
