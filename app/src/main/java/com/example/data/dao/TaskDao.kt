package com.example.data.dao

import androidx.room.*
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, orderIndex ASC, id DESC")
    fun getAllTasks(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE dueDate = :date ORDER BY isCompleted ASC, priority DESC, dueTime ASC, id DESC")
    fun getTasksForDate(date: String): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE dueDate < :todayDate AND isCompleted = 0 ORDER BY dueDate ASC, priority DESC")
    fun getOverdueTasks(todayDate: String): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE dueDate > :todayDate ORDER BY dueDate ASC, priority DESC")
    fun getUpcomingTasks(todayDate: String): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getTaskById(id: Long): Flow<TaskWithSubtasks?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE tasks SET dueDate = :newDate, snoozedUntilDate = NULL WHERE id = :id")
    suspend fun rescheduleTask(id: Long, newDate: String)

    @Query("UPDATE tasks SET dueDate = :todayDate WHERE dueDate < :todayDate AND isCompleted = 0")
    suspend fun moveOverdueToToday(todayDate: String)

    @Query("UPDATE tasks SET focusMinutesSpent = focusMinutesSpent + :minutes WHERE id = :id")
    suspend fun addFocusMinutes(id: Long, minutes: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: Subtask): Long

    @Update
    suspend fun updateSubtask(subtask: Subtask)

    @Delete
    suspend fun deleteSubtask(subtask: Subtask)

    @Query("UPDATE subtasks SET isDone = :isDone WHERE id = :id")
    suspend fun setSubtaskDone(id: Long, isDone: Boolean)
}
