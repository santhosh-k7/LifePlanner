package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import com.example.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

typealias AppDatabase = LifePlannerDatabase

@Database(
    entities = [
        Task::class,
        Subtask::class,
        Category::class,
        Routine::class,
        RoutineStep::class,
        Habit::class,
        HabitLog::class,
        Review::class,
        FocusSession::class,
        Note::class,
        LeetCodeProblem::class
    ],
    version = 4,
    exportSchema = false
)
abstract class LifePlannerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun routineDao(): RoutineDao
    abstract fun habitDao(): HabitDao
    abstract fun reviewDao(): ReviewDao
    abstract fun focusDao(): FocusDao
    abstract fun noteDao(): NoteDao
    abstract fun leetCodeDao(): LeetCodeDao

    companion object {
        @Volatile
        private var INSTANCE: LifePlannerDatabase? = null

        fun getDatabase(context: Context): LifePlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifePlannerDatabase::class.java,
                    "lifeplanner_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedStarterData(database)
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedStarterData(database)
                    }
                }
            }
        }

        suspend fun seedStarterData(database: LifePlannerDatabase) {
            val today = LocalDate.now()
            val todayStr = DateTimeUtils.formatDate(today)
            val yesterdayStr = DateTimeUtils.formatDate(today.minusDays(1))
            val tomorrowStr = DateTimeUtils.formatDate(today.plusDays(1))
            val in3DaysStr = DateTimeUtils.formatDate(today.plusDays(3))

            // 1. Seed Routines (Habits completely removed per request)
            val routine1Id = database.routineDao().insertRoutine(
                Routine(
                    name = "Morning Reset",
                    iconEmoji = "☀️",
                    timeOfDay = "Morning",
                    startTime = "07:30",
                    isTemplate = false
                )
            )
            database.routineDao().insertStep(RoutineStep(routineId = routine1Id, title = "Drink a large glass of water", durationMin = 2, orderIndex = 0))
            database.routineDao().insertStep(RoutineStep(routineId = routine1Id, title = "5-min gentle movement & stretch", durationMin = 5, orderIndex = 1))
            database.routineDao().insertStep(RoutineStep(routineId = routine1Id, title = "Review today's top 3 priorities", durationMin = 3, orderIndex = 2))
            database.routineDao().insertStep(RoutineStep(routineId = routine1Id, title = "Mindful breakfast with tea", durationMin = 15, orderIndex = 3))

            val routine2Id = database.routineDao().insertRoutine(
                Routine(
                    name = "Evening Wind-down",
                    iconEmoji = "🌙",
                    timeOfDay = "Evening",
                    startTime = "21:30",
                    isTemplate = false
                )
            )
            database.routineDao().insertStep(RoutineStep(routineId = routine2Id, title = "Close browser tabs & tidy workspace", durationMin = 4, orderIndex = 0))
            database.routineDao().insertStep(RoutineStep(routineId = routine2Id, title = "Evening reflection & plan tomorrow", durationMin = 5, orderIndex = 1))
            database.routineDao().insertStep(RoutineStep(routineId = routine2Id, title = "Dim lights & read book", durationMin = 20, orderIndex = 2))

            // 2. Seed Overdue Task
            database.taskDao().insertTask(
                Task(
                    title = "Confirm dentist appointment",
                    notes = "Call reception before 5 PM",
                    dueDate = yesterdayStr,
                    priority = Priority.MEDIUM,
                    categoryId = "health",
                    medium = "Phone Call",
                    reminderOffsetMin = 10,
                    isCompleted = false
                )
            )

            // 3. Seed Today Tasks
            val taskId1 = database.taskDao().insertTask(
                Task(
                    title = "Deep Work: Polish app architecture",
                    notes = "Implement clean M3 design and Room reactive flows",
                    dueDate = todayStr,
                    dueTime = "09:30",
                    durationMin = 60,
                    priority = Priority.HIGH,
                    categoryId = "coding",
                    medium = "Desk Work",
                    reminderOffsetMin = 10,
                    isCompleted = false
                )
            )
            database.taskDao().insertSubtask(Subtask(taskId = taskId1, title = "Create entity schemas", isDone = true, orderIndex = 0))
            database.taskDao().insertSubtask(Subtask(taskId = taskId1, title = "Verify DAO reactive queries", isDone = true, orderIndex = 1))
            database.taskDao().insertSubtask(Subtask(taskId = taskId1, title = "Build Focus Timer view", isDone = false, orderIndex = 2))

            database.taskDao().insertTask(
                Task(
                    title = "Review Q4 product roadmap",
                    notes = "Align team deliverables with milestones",
                    dueDate = todayStr,
                    dueTime = "11:00",
                    durationMin = 45,
                    priority = Priority.HIGH,
                    categoryId = "work",
                    medium = "Meeting / Zoom",
                    reminderOffsetMin = 30,
                    isCompleted = false
                )
            )

            database.taskDao().insertTask(
                Task(
                    title = "Prepare healthy lunch",
                    notes = "Steamed greens, quinoa bowl",
                    dueDate = todayStr,
                    dueTime = "13:00",
                    durationMin = 30,
                    priority = Priority.MEDIUM,
                    categoryId = "health",
                    medium = "In-Person",
                    reminderOffsetMin = 0,
                    isCompleted = false
                )
            )

            database.taskDao().insertTask(
                Task(
                    title = "Grocery run: fresh produce & pantry staples",
                    notes = "Apples, spinach, sourdough, oats",
                    dueDate = todayStr,
                    priority = Priority.LOW,
                    categoryId = "personal",
                    medium = "In-Person",
                    reminderOffsetMin = null,
                    isCompleted = false
                )
            )

            database.taskDao().insertTask(
                Task(
                    title = "Quick morning stretch",
                    notes = "Hamstrings and spine mobility",
                    dueDate = todayStr,
                    dueTime = "07:15",
                    durationMin = 15,
                    priority = Priority.MEDIUM,
                    categoryId = "health",
                    medium = "Desk Work",
                    reminderOffsetMin = 0,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis() - 7200000
                )
            )

            // 4. Seed Upcoming Tasks
            database.taskDao().insertTask(
                Task(
                    title = "Design team sprint retrospective",
                    notes = "Celebrate wins and discuss blockers",
                    dueDate = tomorrowStr,
                    dueTime = "10:30",
                    durationMin = 45,
                    priority = Priority.MEDIUM,
                    categoryId = "work",
                    medium = "Online",
                    reminderOffsetMin = 10
                )
            )

            database.taskDao().insertTask(
                Task(
                    title = "Car maintenance & tire check",
                    notes = "Service center on 5th avenue",
                    dueDate = in3DaysStr,
                    dueTime = "14:00",
                    durationMin = 60,
                    priority = Priority.LOW,
                    categoryId = "personal",
                    medium = "In-Person",
                    reminderOffsetMin = null
                )
            )

            // 6. Seed Focus session log
            database.focusDao().insertSession(
                FocusSession(
                    taskTitle = "Deep Work: Polish app architecture",
                    durationSeconds = 1500,
                    sessionType = "POMODORO_FOCUS"
                )
            )

            // 7. Seed Notepad (Urgent / Important notes)
            database.noteDao().insertNote(
                Note(
                    title = "🚨 System Launch Checklist",
                    content = "1. Verify Room DB migration hash\n2. Test alarm notifications on device\n3. Double check dark AMOLED contrast\n4. Confirm zero text clipping on compact screens",
                    isUrgent = true,
                    isImportant = true,
                    isPinned = true,
                    tag = "Launch",
                    colorHex = 0xFFEF4444
                )
            )
            database.noteDao().insertNote(
                Note(
                    title = "⭐ Architecture Refactor Notes",
                    content = "Liquid Glass aesthetic relies on subtle refractive gradient borders (1dp), frosted surface alphas (0.65f), and specular highlight passes.",
                    isUrgent = false,
                    isImportant = true,
                    isPinned = true,
                    tag = "Engineering",
                    colorHex = 0xFF6366F1
                )
            )
            database.noteDao().insertNote(
                Note(
                    title = "⚡ Meeting summary: Client sync",
                    content = "Deliverable timelines moved up by 3 days. Send finalized API contract before tomorrow morning.",
                    isUrgent = true,
                    isImportant = false,
                    isPinned = false,
                    tag = "Work",
                    colorHex = 0xFFF59E0B
                )
            )

            // 8. Seed LeetCode Progress
            database.leetCodeDao().insertProblem(
                LeetCodeProblem(
                    problemNumber = 1,
                    title = "Two Sum",
                    difficulty = "Easy",
                    topic = "Array & Hash Table",
                    dateSolved = todayStr,
                    notes = "Single pass with HashMap storage of complement: O(N) time and O(N) space.",
                    timeSpentMinutes = 10
                )
            )
            database.leetCodeDao().insertProblem(
                LeetCodeProblem(
                    problemNumber = 146,
                    title = "LRU Cache",
                    difficulty = "Medium",
                    topic = "Design & Doubly Linked List",
                    dateSolved = todayStr,
                    notes = "HashMap + DoublyLinkedList for O(1) get and put. Always update node to MRU on access.",
                    timeSpentMinutes = 25
                )
            )
            database.leetCodeDao().insertProblem(
                LeetCodeProblem(
                    problemNumber = 42,
                    title = "Trapping Rain Water",
                    difficulty = "Hard",
                    topic = "Two Pointers & Monotonic Stack",
                    dateSolved = yesterdayStr,
                    notes = "Two pointers approach maintaining leftMax and rightMax bounds. O(1) extra space.",
                    timeSpentMinutes = 35
                )
            )
            database.leetCodeDao().insertProblem(
                LeetCodeProblem(
                    problemNumber = 200,
                    title = "Number of Islands",
                    difficulty = "Medium",
                    topic = "Graph & BFS/DFS",
                    dateSolved = yesterdayStr,
                    notes = "Sink visited land cells or maintain boolean visited grid. BFS avoids recursion depth limits.",
                    timeSpentMinutes = 18
                )
            )
        }
    }
}
