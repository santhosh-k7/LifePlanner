package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.LifePlannerDatabase
import com.example.data.model.Priority
import com.example.data.model.Task
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TaskRepositoryTest {

    private lateinit var database: LifePlannerDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            LifePlannerDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = TaskRepository(database.taskDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveTask() = runBlocking {
        val task = Task(
            title = "Test Architecture Implementation",
            notes = "Verify Room Entity, DAO, and Repository",
            dueDate = "12-10-2026",
            dueTime = "14:00",
            durationMin = 45,
            priority = Priority.HIGH,
            categoryId = "work",
            medium = "Online"
        )

        val insertedId = repository.insertTask(task, listOf("Verify Entity", "Verify DAO", "Verify Repository"))
        assertTrue(insertedId > 0)

        // Retrieve through repository flow
        val retrieved = repository.getTaskById(insertedId).first()
        assertNotNull(retrieved)
        assertEquals("Test Architecture Implementation", retrieved?.task?.title)
        assertEquals(Priority.HIGH, retrieved?.task?.priority)
        assertEquals("Online", retrieved?.task?.medium)
        assertEquals(3, retrieved?.subtasks?.size)

        // Test toggle complete
        repository.setTaskCompleted(insertedId, true)
        val updated = repository.getTaskById(insertedId).first()
        assertTrue(updated?.task?.isCompleted == true)
        assertNotNull(updated?.task?.completedAt)
    }

    @Test
    fun testDeleteTask() = runBlocking {
        val task = Task(
            title = "Task to be deleted",
            dueDate = "12-10-2026"
        )
        val id = repository.insertTask(task)
        assertNotNull(repository.getTaskByIdDirect(id))

        repository.deleteTaskById(id)
        val all = repository.allTasks.first()
        assertTrue(all.none { it.task.id == id })
    }

    @Test
    fun testMultipleNotificationTimings() = runBlocking {
        val task = Task(
            title = "Team Sprint Planning Event",
            dueDate = "12-10-2026",
            dueTime = "15:00",
            reminderOffsets = "0,15,60"
        )
        val id = repository.insertTask(task)
        val retrieved = repository.getTaskById(id).first()
        assertNotNull(retrieved)
        val offsets = retrieved!!.task.getReminderOffsetList()
        assertEquals(listOf(0, 15, 60), offsets)
    }
}
