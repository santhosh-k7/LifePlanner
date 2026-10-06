package com.example

import com.example.data.model.HabitLog
import com.example.data.model.Priority
import com.example.data.model.RepeatRule
import com.example.util.DateTimeUtils
import com.example.util.NaturalLanguageParser
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun testNaturalLanguageParser_allTokens() {
        val baseDate = LocalDate.of(2026, 10, 6)
        val input = "Gym tomorrow 6pm !high #health"
        val draft = NaturalLanguageParser.parse(input, baseDate)

        assertEquals("Gym", draft.title)
        assertEquals("2026-10-07", draft.dueDate)
        assertEquals("18:00", draft.dueTime)
        assertEquals(Priority.HIGH, draft.priority)
        assertEquals("health", draft.categoryId)
    }

    @Test
    fun testNaturalLanguageParser_inDaysAndWork() {
        val baseDate = LocalDate.of(2026, 10, 6)
        val input = "Design sync in 2 days 15:30 !med #work"
        val draft = NaturalLanguageParser.parse(input, baseDate)

        assertEquals("Design sync", draft.title)
        assertEquals("2026-10-08", draft.dueDate)
        assertEquals("15:30", draft.dueTime)
        assertEquals(Priority.MEDIUM, draft.priority)
        assertEquals("work", draft.categoryId)
    }

    @Test
    fun testNaturalLanguageParser_recurringDaily() {
        val baseDate = LocalDate.of(2026, 10, 6)
        val input = "Morning meditation every day 7am !low"
        val draft = NaturalLanguageParser.parse(input, baseDate)

        assertEquals("Morning meditation", draft.title)
        assertEquals(RepeatRule.DAILY, draft.repeatRule)
        assertEquals("07:00", draft.dueTime)
        assertEquals(Priority.LOW, draft.priority)
    }

    @Test
    fun testNaturalLanguageParser_otherCategory() {
        val baseDate = LocalDate.of(2026, 10, 6)
        val input = "Pay internet bill tomorrow !med #other"
        val draft = NaturalLanguageParser.parse(input, baseDate)

        assertEquals("Pay internet bill", draft.title)
        assertEquals("other", draft.categoryId)
    }

    @Test
    fun testNaturalLanguageParser_othersCategory() {
        val baseDate = LocalDate.of(2026, 10, 6)
        val input = "Send feedback tomorrow !low #others"
        val draft = NaturalLanguageParser.parse(input, baseDate)

        assertEquals("Send feedback", draft.title)
        assertEquals("others", draft.categoryId)
    }

    @Test
    fun testNaturalLanguageParser_reminderOffset() {
        val baseDate = LocalDate.of(2026, 10, 6)
        val input10 = "Doctor appointment tomorrow 3pm 10m before !high"
        val draft10 = NaturalLanguageParser.parse(input10, baseDate)
        assertEquals(10, draft10.reminderOffsetMin)

        val input30 = "Team sync tomorrow 11am 30 mins before"
        val draft30 = NaturalLanguageParser.parse(input30, baseDate)
        assertEquals(30, draft30.reminderOffsetMin)

        val inputOnTime = "Flight check-in tomorrow 9am on time"
        val draftOnTime = NaturalLanguageParser.parse(inputOnTime, baseDate)
        assertEquals(0, draftOnTime.reminderOffsetMin)
    }

    @Test
    fun testHabitStreakCalculation_withForgivingGraceDay() {
        val today = LocalDate.now()
        val logs = listOf(
            HabitLog(habitId = 1, date = today.toString(), isCompleted = true),
            // missed yesterday! But grace day covers single missed day
            HabitLog(habitId = 1, date = today.minusDays(2).toString(), isCompleted = true),
            HabitLog(habitId = 1, date = today.minusDays(3).toString(), isCompleted = true)
        )

        val (currentStreak, bestStreak) = DateTimeUtils.calculateHabitStreaks(logs, graceDaysPerWeek = 1)
        assertTrue("Streak should not be broken because of forgiving grace day", currentStreak >= 2)
    }

    @Test
    fun testFutureDateValidation() {
        val today = LocalDate.now()
        val future = today.plusDays(2)
        assertTrue(future.isAfter(today))
        assertFalse(today.isAfter(today))
    }
}
