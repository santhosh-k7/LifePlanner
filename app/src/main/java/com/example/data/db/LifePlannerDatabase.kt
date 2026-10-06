package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

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
        FocusSession::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LifePlannerDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun routineDao(): RoutineDao
    abstract fun habitDao(): HabitDao
    abstract fun reviewDao(): ReviewDao
    abstract fun focusDao(): FocusDao

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
            val todayStr = today.toString()
            val yesterdayStr = today.minusDays(1).toString()
            val tomorrowStr = today.plusDays(1).toString()
            val in3DaysStr = today.plusDays(3).toString()

            // 1. Seed Habits
            val habit1 = database.habitDao().insertHabit(
                Habit(name = "Drink 2L Water", iconEmoji = "💧", targetDaysPerWeek = 7, colorHex = 0xFF0D9488)
            )
            val habit2 = database.habitDao().insertHabit(
                Habit(name = "Morning Walk", iconEmoji = "🚶", targetDaysPerWeek = 5, colorHex = 0xFF10B981)
            )
            val habit3 = database.habitDao().insertHabit(
                Habit(name = "Read 20 Mins", iconEmoji = "📖", targetDaysPerWeek = 6, colorHex = 0xFF8B5CF6)
            )
            val habit4 = database.habitDao().insertHabit(
                Habit(name = "Mindful Journal", iconEmoji = "✨", targetDaysPerWeek = 7, colorHex = 0xFFF59E0B)
            )

            // Seed some past habit logs for streaks
            database.habitDao().logHabit(HabitLog(habitId = habit1, date = yesterdayStr))
            database.habitDao().logHabit(HabitLog(habitId = habit2, date = yesterdayStr))
            database.habitDao().logHabit(HabitLog(habitId = habit3, date = yesterdayStr))

            // 2. Seed Routines
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

            // 3. Seed Overdue Task
            database.taskDao().insertTask(
                Task(
                    title = "Confirm dentist appointment",
                    notes = "Call reception before 5 PM",
                    dueDate = yesterdayStr,
                    priority = Priority.MEDIUM,
                    categoryId = "health",
                    isCompleted = false
                )
            )

            // 4. Seed Today Tasks
            val taskId1 = database.taskDao().insertTask(
                Task(
                    title = "Deep Work: Polish app architecture",
                    notes = "Implement clean M3 design and Room reactive flows",
                    dueDate = todayStr,
                    dueTime = "09:30",
                    durationMin = 60,
                    priority = Priority.HIGH,
                    categoryId = "learning",
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
                    isCompleted = true,
                    completedAt = System.currentTimeMillis() - 7200000
                )
            )

            // 5. Seed Upcoming Tasks
            database.taskDao().insertTask(
                Task(
                    title = "Design team sprint retrospective",
                    notes = "Celebrate wins and discuss blockers",
                    dueDate = tomorrowStr,
                    dueTime = "10:30",
                    durationMin = 45,
                    priority = Priority.MEDIUM,
                    categoryId = "work"
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
                    categoryId = "personal"
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
        }
    }
}
