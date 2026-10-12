package com.example.data.repository

import com.example.data.dao.TaskDao
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskWithSubtasks
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow

/**
 * Repository class to manage task data operations, bridging the Room database
 * (via [TaskDao]) with the ViewModel layer.
 */
class TaskRepository(
    private val taskDao: TaskDao
) {

    /**
     * Observable flow of all tasks with their subtasks.
     */
    val allTasks: Flow<List<TaskWithSubtasks>> = taskDao.getAllTasks()

    /**
     * Observable flow of all raw task entities.
     */
    val allRawTasks: Flow<List<Task>> = taskDao.getAllRawTasks()

    /**
     * Observable flow of tasks due today.
     */
    val todayTasks: Flow<List<TaskWithSubtasks>> = taskDao.getTasksForDate(DateTimeUtils.todayStr())

    /**
     * Observable flow of uncompleted overdue tasks.
     */
    val overdueTasks: Flow<List<TaskWithSubtasks>> = taskDao.getOverdueTasks(DateTimeUtils.todayStr())

    /**
     * Observable flow of upcoming future tasks.
     */
    val upcomingTasks: Flow<List<TaskWithSubtasks>> = taskDao.getUpcomingTasks(DateTimeUtils.todayStr())

    /**
     * Retrieve tasks for a specific date string (dd-MM-yyyy or yyyy-MM-dd).
     */
    fun getTasksForDate(date: String): Flow<List<TaskWithSubtasks>> = taskDao.getTasksForDate(date)

    /**
     * Retrieve raw tasks for a specific date string.
     */
    fun getRawTasksForDate(date: String): Flow<List<Task>> = taskDao.getRawTasksForDate(date)

    /**
     * Retrieve a specific task by its unique ID.
     */
    fun getTaskById(id: Long): Flow<TaskWithSubtasks?> = taskDao.getTaskById(id)

    /**
     * Retrieve a raw task entity by ID.
     */
    fun getRawTaskById(id: Long): Flow<Task?> = taskDao.getRawTaskById(id)

    /**
     * Retrieve a task directly (one-shot suspend function).
     */
    suspend fun getTaskByIdDirect(id: Long): Task? = taskDao.getTaskByIdDirect(id)

    /**
     * Insert a new task and optionally its child subtasks.
     * Returns the newly generated task ID.
     */
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

    /**
     * Batch insert multiple tasks.
     */
    suspend fun insertTasks(tasks: List<Task>): List<Long> = taskDao.insertTasks(tasks)

    /**
     * Update an existing task.
     */
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    /**
     * Delete an existing task entity.
     */
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    /**
     * Delete a task by its ID.
     */
    suspend fun deleteTaskById(taskId: Long) = taskDao.deleteTaskById(taskId)

    /**
     * Set a task's completion state and timestamp.
     */
    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        taskDao.setTaskCompleted(taskId, isCompleted, completedAt)
    }

    /**
     * Toggle a task's completion status.
     */
    suspend fun toggleTaskCompleted(taskId: Long, currentCompleted: Boolean) {
        setTaskCompleted(taskId, !currentCompleted)
    }

    /**
     * Reschedule a task to a new date.
     */
    suspend fun rescheduleTask(taskId: Long, newDate: String) {
        taskDao.rescheduleTask(taskId, newDate)
    }

    /**
     * Move all overdue uncompleted tasks to today.
     */
    suspend fun moveAllOverdueToToday(todayDate: String = DateTimeUtils.todayStr()) {
        taskDao.moveOverdueToToday(todayDate)
    }

    /**
     * Add focus minutes spent on a task.
     */
    suspend fun addFocusMinutes(taskId: Long, minutes: Int) {
        taskDao.addFocusMinutes(taskId, minutes)
    }

    /**
     * Insert a subtask for a given task.
     */
    suspend fun addSubtask(taskId: Long, title: String): Long {
        return taskDao.insertSubtask(Subtask(taskId = taskId, title = title.trim()))
    }

    /**
     * Toggle subtask done status.
     */
    suspend fun setSubtaskDone(subtaskId: Long, isDone: Boolean) {
        taskDao.setSubtaskDone(subtaskId, isDone)
    }

    /**
     * Toggle subtask completion status from current state.
     */
    suspend fun toggleSubtask(subtaskId: Long, currentDone: Boolean) {
        taskDao.setSubtaskDone(subtaskId, !currentDone)
    }

    /**
     * Delete a subtask entity.
     */
    suspend fun deleteSubtask(subtask: Subtask) {
        taskDao.deleteSubtask(subtask)
    }
}
