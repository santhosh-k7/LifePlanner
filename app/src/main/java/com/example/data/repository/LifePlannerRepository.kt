package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class LifePlannerRepository(
    private val taskDao: TaskDao,
    private val routineDao: RoutineDao,
    private val habitDao: HabitDao,
    private val reviewDao: ReviewDao,
    private val focusDao: FocusDao
) {
    val allTasks: Flow<List<TaskWithSubtasks>> = taskDao.getAllTasks()
    val todayTasks: Flow<List<TaskWithSubtasks>> = taskDao.getTasksForDate(DateTimeUtils.todayStr())
    val overdueTasks: Flow<List<TaskWithSubtasks>> = taskDao.getOverdueTasks(DateTimeUtils.todayStr())
    val upcomingTasks: Flow<List<TaskWithSubtasks>> = taskDao.getUpcomingTasks(DateTimeUtils.todayStr())

    val allRoutines: Flow<List<RoutineWithSteps>> = routineDao.getAllRoutines()
    val allHabits: Flow<List<HabitWithLogs>> = habitDao.getAllHabits()
    val allReviews: Flow<List<Review>> = reviewDao.getAllReviews()
    val allFocusSessions: Flow<List<FocusSession>> = focusDao.getAllSessions()
    val totalFocusSeconds: Flow<Long?> = focusDao.getTotalFocusSeconds()

    fun getTasksForDate(date: String): Flow<List<TaskWithSubtasks>> = taskDao.getTasksForDate(date)

    suspend fun insertTask(task: Task, subtaskTitles: List<String> = emptyList()): Long {
        val taskId = taskDao.insertTask(task)
        subtaskTitles.forEachIndexed { index, title ->
            if (title.isNotBlank()) {
                taskDao.insertSubtask(
                    Subtask(
                        taskId = taskId,
                        title = title.trim(),
                        isDone = false,
                        orderIndex = index
                    )
                )
            }
        }
        return taskId
    }

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(taskId: Long) = taskDao.deleteTaskById(taskId)

    suspend fun toggleTaskCompleted(taskId: Long, currentCompleted: Boolean) {
        val newStatus = !currentCompleted
        val completedAt = if (newStatus) System.currentTimeMillis() else null
        taskDao.setTaskCompleted(taskId, newStatus, completedAt)
    }

    suspend fun rescheduleTask(taskId: Long, newDate: String) {
        taskDao.rescheduleTask(taskId, newDate)
    }

    suspend fun moveAllOverdueToToday() {
        taskDao.moveOverdueToToday(DateTimeUtils.todayStr())
    }

    suspend fun addFocusMinutes(taskId: Long, minutes: Int) {
        taskDao.addFocusMinutes(taskId, minutes)
    }

    suspend fun addSubtask(taskId: Long, title: String) {
        taskDao.insertSubtask(Subtask(taskId = taskId, title = title.trim()))
    }

    suspend fun toggleSubtask(subtaskId: Long, currentDone: Boolean) {
        taskDao.setSubtaskDone(subtaskId, !currentDone)
    }

    suspend fun deleteSubtask(subtask: Subtask) {
        taskDao.deleteSubtask(subtask)
    }

    // Routines
    suspend fun insertRoutine(routine: Routine, stepTitles: List<Pair<String, Int>> = emptyList()): Long {
        val routineId = routineDao.insertRoutine(routine)
        stepTitles.forEachIndexed { index, (title, duration) ->
            routineDao.insertStep(
                RoutineStep(
                    routineId = routineId,
                    title = title,
                    durationMin = duration,
                    orderIndex = index
                )
            )
        }
        return routineId
    }

    suspend fun updateRoutine(routine: Routine) = routineDao.updateRoutine(routine)

    suspend fun deleteRoutine(routine: Routine) = routineDao.deleteRoutine(routine)

    suspend fun markRoutineStepDone(stepId: Long, isDone: Boolean) {
        val date = if (isDone) DateTimeUtils.todayStr() else null
        routineDao.markStepCompleted(stepId, date)
    }

    suspend fun resetRoutineForToday(routineId: Long) {
        routineDao.resetRoutineStepsForDay(routineId)
    }

    // Habits
    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun toggleHabitLog(habitId: Long, date: String = DateTimeUtils.todayStr()) {
        try {
            val targetDate = java.time.LocalDate.parse(date)
            if (targetDate.isAfter(java.time.LocalDate.now())) {
                return // Cannot complete habit in the future
            }
        } catch (_: Exception) {}

        val count = habitDao.isHabitCompletedOnDate(habitId, date)
        if (count > 0) {
            habitDao.unlogHabit(habitId, date)
        } else {
            habitDao.logHabit(HabitLog(habitId = habitId, date = date, isCompleted = true))
        }
    }

    // Reviews
    suspend fun insertReview(review: Review) = reviewDao.insertReview(review)

    // Focus
    suspend fun logFocusSession(session: FocusSession) = focusDao.insertSession(session)
}
