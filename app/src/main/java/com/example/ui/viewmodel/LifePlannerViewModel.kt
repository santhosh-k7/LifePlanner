package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.LifePlannerDatabase
import com.example.data.model.*
import com.example.data.repository.LifePlannerRepository
import com.example.util.DateTimeUtils
import com.example.util.NaturalLanguageParser
import com.example.util.ParsedTaskDraft
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class AppTab(val title: String) {
    TODAY("Today"),
    UPCOMING("Upcoming"),
    ROUTINES("Routines"),
    REVIEW("Review")
}

enum class PomodoroPhase(val title: String, val defaultMinutes: Int) {
    FOCUS("Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15),
    STOPWATCH("Stopwatch", 0)
}

class LifePlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LifePlannerRepository

    init {
        val db = LifePlannerDatabase.getDatabase(application)
        repository = LifePlannerRepository(
            taskDao = db.taskDao(),
            routineDao = db.routineDao(),
            habitDao = db.habitDao(),
            reviewDao = db.reviewDao(),
            focusDao = db.focusDao()
        )
    }

    // Navigation & UI State
    val currentTab = MutableStateFlow(AppTab.TODAY)
    val isSearchActive = MutableStateFlow(false)
    val searchQuery = MutableStateFlow("")

    // Settings
    val themeMode = MutableStateFlow(0) // 0: System, 1: Light, 2: Dark
    val showSettingsDialog = MutableStateFlow(false)
    val pomodoroFocusMinutes = MutableStateFlow(25)
    val pomodoroBreakMinutes = MutableStateFlow(5)

    // Data Flows from Room
    val allTasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val todayTasks = repository.todayTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val overdueTasks = repository.overdueTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val upcomingTasks = repository.upcomingTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allRoutines = repository.allRoutines.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allHabits = repository.allHabits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allReviews = repository.allReviews.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalFocusSeconds = repository.totalFocusSeconds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Quick Add
    val showQuickAdd = MutableStateFlow(false)
    val quickAddInput = MutableStateFlow("")
    val quickAddDraft = MutableStateFlow(NaturalLanguageParser.parse(""))
    val quickAddOverrideDate = MutableStateFlow<String?>(null)
    val quickAddOverridePriority = MutableStateFlow<Priority?>(null)
    val quickAddOverrideCategory = MutableStateFlow<String?>(null)

    // Task Detail
    val selectedTaskForDetail = MutableStateFlow<TaskWithSubtasks?>(null)

    // Reschedule / Snooze
    val taskToReschedule = MutableStateFlow<Task?>(null)

    // Focus Timer State
    val isFocusScreenVisible = MutableStateFlow(false)
    val focusTask = MutableStateFlow<Task?>(null)
    val focusPhase = MutableStateFlow(PomodoroPhase.FOCUS)
    val focusRound = MutableStateFlow(1)
    val timerRemainingSeconds = MutableStateFlow(25 * 60)
    val timerTotalSeconds = MutableStateFlow(25 * 60)
    val isTimerActive = MutableStateFlow(false)
    private var timerJob: Job? = null

    // Routine Runner
    val activeRunningRoutine = MutableStateFlow<RoutineWithSteps?>(null)
    val currentRoutineStepIndex = MutableStateFlow(0)
    val routineStepRemainingSeconds = MutableStateFlow(0)
    val isRoutineTimerActive = MutableStateFlow(false)
    val showRoutineFinishedDialog = MutableStateFlow(false)
    private var routineTimerJob: Job? = null

    // Daily & Weekly Review Flow
    val showDailyReviewModal = MutableStateFlow(false)
    val dailyReviewReflection = MutableStateFlow("")

    val showWeeklyReviewModal = MutableStateFlow(false)
    val weeklyReviewStep = MutableStateFlow(1)
    val weeklyReviewWell = MutableStateFlow("")
    val weeklyReviewImprove = MutableStateFlow("")
    val weeklyPriority1 = MutableStateFlow("")
    val weeklyPriority2 = MutableStateFlow("")
    val weeklyPriority3 = MutableStateFlow("")

    // Smart Suggestion
    val isSmartSuggestionDismissed = MutableStateFlow(false)

    // Habits Subtab vs Routines Subtab
    val routinesHabitsSubTab = MutableStateFlow(0) // 0: Routines, 1: Habits
    val upcomingViewMode = MutableStateFlow(0) // 0: List, 1: Weekly Planner

    // Haptic Feedback Helper
    fun performHapticFeedback() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}
    }

    // Quick Add Handling
    fun updateQuickAddInput(text: String) {
        quickAddInput.value = text
        val parsed = NaturalLanguageParser.parse(text)
        quickAddDraft.value = parsed
    }

    fun openQuickAdd(initialText: String = "", initialDate: String? = null) {
        quickAddInput.value = initialText
        quickAddOverrideDate.value = initialDate
        quickAddOverridePriority.value = null
        quickAddOverrideCategory.value = null
        quickAddDraft.value = NaturalLanguageParser.parse(initialText)
        showQuickAdd.value = true
    }

    fun submitQuickAdd(stayOpen: Boolean = false) {
        val text = quickAddInput.value.trim()
        val draft = quickAddDraft.value
        if (draft.title.isBlank() && text.isBlank()) return

        val finalTitle = if (draft.title.isNotBlank()) draft.title else text
        val finalDate = quickAddOverrideDate.value ?: draft.dueDate
        val finalPriority = quickAddOverridePriority.value ?: draft.priority
        val finalCategory = quickAddOverrideCategory.value ?: draft.categoryId

        viewModelScope.launch {
            repository.insertTask(
                Task(
                    title = finalTitle,
                    dueDate = finalDate,
                    dueTime = draft.dueTime,
                    durationMin = draft.durationMin,
                    priority = finalPriority,
                    categoryId = finalCategory,
                    repeatRule = draft.repeatRule
                )
            )
            performHapticFeedback()
            if (stayOpen) {
                quickAddInput.value = ""
                quickAddDraft.value = NaturalLanguageParser.parse("")
                quickAddOverridePriority.value = null
                quickAddOverrideCategory.value = null
            } else {
                showQuickAdd.value = false
                quickAddInput.value = ""
            }
        }
    }

    // Task Actions
    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task.id, task.isCompleted)
            performHapticFeedback()

            // If recurring and completing, generate next occurrence
            if (!task.isCompleted && task.repeatRule != RepeatRule.NONE) {
                val nextDate = try {
                    val curr = LocalDate.parse(task.dueDate)
                    when (task.repeatRule) {
                        RepeatRule.DAILY -> curr.plusDays(1).toString()
                        RepeatRule.WEEKLY -> curr.plusWeeks(1).toString()
                        RepeatRule.MONTHLY -> curr.plusMonths(1).toString()
                        else -> null
                    }
                } catch (e: Exception) { null }

                if (nextDate != null) {
                    repository.insertTask(
                        task.copy(
                            id = 0,
                            dueDate = nextDate,
                            isCompleted = false,
                            completedAt = null
                        )
                    )
                }
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            if (selectedTaskForDetail.value?.task?.id == taskId) {
                selectedTaskForDetail.value = null
            }
        }
    }

    fun rescheduleTask(taskId: Long, newDate: String) {
        viewModelScope.launch {
            repository.rescheduleTask(taskId, newDate)
            taskToReschedule.value = null
        }
    }

    fun moveAllOverdueToToday() {
        viewModelScope.launch {
            repository.moveAllOverdueToToday()
            performHapticFeedback()
        }
    }

    fun addSubtask(taskId: Long, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addSubtask(taskId, title)
        }
    }

    fun toggleSubtask(subtask: Subtask) {
        viewModelScope.launch {
            repository.toggleSubtask(subtask.id, subtask.isDone)
            performHapticFeedback()
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch {
            repository.deleteSubtask(subtask)
        }
    }

    fun updateTaskDetails(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            selectedTaskForDetail.value = null
        }
    }

    // Habit Actions
    fun toggleHabitToday(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitLog(habitId, DateTimeUtils.todayStr())
            performHapticFeedback()
        }
    }

    fun toggleHabitOnDate(habitId: Long, date: String) {
        viewModelScope.launch {
            repository.toggleHabitLog(habitId, date)
            performHapticFeedback()
        }
    }

    fun addHabit(name: String, icon: String, targetDays: Int, graceDays: Int, colorHex: Long) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertHabit(
                Habit(
                    name = name.trim(),
                    iconEmoji = icon,
                    targetDaysPerWeek = targetDays,
                    graceDaysPerWeek = graceDays,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Routine Actions
    fun markRoutineStepDone(stepId: Long, isDone: Boolean) {
        viewModelScope.launch {
            repository.markRoutineStepDone(stepId, isDone)
            performHapticFeedback()
        }
    }

    fun addRoutine(name: String, icon: String, timeOfDay: String, startTime: String, steps: List<Pair<String, Int>>) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertRoutine(
                Routine(name = name.trim(), iconEmoji = icon, timeOfDay = timeOfDay, startTime = startTime),
                steps
            )
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
        }
    }

    // Routine Runner
    fun startRoutineRunner(routine: RoutineWithSteps) {
        activeRunningRoutine.value = routine
        currentRoutineStepIndex.value = 0
        showRoutineFinishedDialog.value = false
        val firstStep = routine.steps.getOrNull(0)
        routineStepRemainingSeconds.value = (firstStep?.durationMin ?: 2) * 60
        isRoutineTimerActive.value = true
        startRoutineTimer()
    }

    private fun startRoutineTimer() {
        routineTimerJob?.cancel()
        routineTimerJob = viewModelScope.launch {
            while (isRoutineTimerActive.value && routineStepRemainingSeconds.value > 0) {
                delay(1000)
                routineStepRemainingSeconds.value -= 1
            }
            if (routineStepRemainingSeconds.value <= 0) {
                performHapticFeedback()
            }
        }
    }

    fun pauseResumeRoutineTimer() {
        val current = isRoutineTimerActive.value
        isRoutineTimerActive.value = !current
        if (!current) {
            startRoutineTimer()
        } else {
            routineTimerJob?.cancel()
        }
    }

    fun nextRoutineStep() {
        val routine = activeRunningRoutine.value ?: return
        val currentStep = routine.steps.getOrNull(currentRoutineStepIndex.value)
        if (currentStep != null) {
            markRoutineStepDone(currentStep.id, true)
        }

        if (currentRoutineStepIndex.value < routine.steps.size - 1) {
            currentRoutineStepIndex.value += 1
            val nextStep = routine.steps[currentRoutineStepIndex.value]
            routineStepRemainingSeconds.value = (nextStep.durationMin) * 60
            isRoutineTimerActive.value = true
            startRoutineTimer()
        } else {
            // Done!
            routineTimerJob?.cancel()
            isRoutineTimerActive.value = false
            showRoutineFinishedDialog.value = true
            performHapticFeedback()
        }
    }

    fun closeRoutineRunner() {
        routineTimerJob?.cancel()
        activeRunningRoutine.value = null
        isRoutineTimerActive.value = false
        showRoutineFinishedDialog.value = false
    }

    // Focus Mode / Pomodoro
    fun openFocusMode(task: Task? = null) {
        focusTask.value = task
        focusPhase.value = PomodoroPhase.FOCUS
        val minutes = pomodoroFocusMinutes.value
        timerTotalSeconds.value = minutes * 60
        timerRemainingSeconds.value = minutes * 60
        isTimerActive.value = true
        isFocusScreenVisible.value = true
        startFocusTimer()
    }

    private fun startFocusTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isTimerActive.value && timerRemainingSeconds.value > 0) {
                delay(1000)
                timerRemainingSeconds.value -= 1
            }
            if (timerRemainingSeconds.value <= 0) {
                onFocusTimerFinished()
            }
        }
    }

    fun pauseResumeFocusTimer() {
        val current = isTimerActive.value
        isTimerActive.value = !current
        if (!current) {
            startFocusTimer()
        } else {
            timerJob?.cancel()
        }
    }

    fun resetFocusTimer() {
        timerJob?.cancel()
        isTimerActive.value = false
        val mins = when (focusPhase.value) {
            PomodoroPhase.FOCUS -> pomodoroFocusMinutes.value
            PomodoroPhase.SHORT_BREAK -> pomodoroBreakMinutes.value
            PomodoroPhase.LONG_BREAK -> 15
            PomodoroPhase.STOPWATCH -> 0
        }
        timerTotalSeconds.value = mins * 60
        timerRemainingSeconds.value = mins * 60
    }

    private fun onFocusTimerFinished() {
        performHapticFeedback()
        val phase = focusPhase.value
        val spentSeconds = timerTotalSeconds.value

        // Log session
        viewModelScope.launch {
            repository.logFocusSession(
                FocusSession(
                    taskId = focusTask.value?.id,
                    taskTitle = focusTask.value?.title ?: "Free Focus",
                    durationSeconds = spentSeconds,
                    sessionType = phase.name
                )
            )
            if (phase == PomodoroPhase.FOCUS) {
                focusTask.value?.let { t ->
                    repository.addFocusMinutes(t.id, spentSeconds / 60)
                }
            }
        }

        // Advance Pomodoro phase
        if (phase == PomodoroPhase.FOCUS) {
            val nextRound = focusRound.value
            if (nextRound % 4 == 0) {
                focusPhase.value = PomodoroPhase.LONG_BREAK
                timerTotalSeconds.value = 15 * 60
                timerRemainingSeconds.value = 15 * 60
            } else {
                focusPhase.value = PomodoroPhase.SHORT_BREAK
                timerTotalSeconds.value = pomodoroBreakMinutes.value * 60
                timerRemainingSeconds.value = pomodoroBreakMinutes.value * 60
            }
            focusRound.value = nextRound + 1
        } else {
            focusPhase.value = PomodoroPhase.FOCUS
            timerTotalSeconds.value = pomodoroFocusMinutes.value * 60
            timerRemainingSeconds.value = pomodoroFocusMinutes.value * 60
        }
        isTimerActive.value = false
    }

    fun completeTaskFromFocus() {
        val task = focusTask.value
        val spent = (timerTotalSeconds.value - timerRemainingSeconds.value)
        viewModelScope.launch {
            if (task != null) {
                repository.toggleTaskCompleted(task.id, false)
                if (spent > 60) {
                    repository.addFocusMinutes(task.id, spent / 60)
                    repository.logFocusSession(
                        FocusSession(
                            taskId = task.id,
                            taskTitle = task.title,
                            durationSeconds = spent,
                            sessionType = "POMODORO_FOCUS"
                        )
                    )
                }
            }
            performHapticFeedback()
            closeFocusScreen()
        }
    }

    fun closeFocusScreen() {
        timerJob?.cancel()
        isTimerActive.value = false
        isFocusScreenVisible.value = false
    }

    // Daily Review
    fun submitDailyReview() {
        val todayStr = DateTimeUtils.todayStr()
        val tasks = todayTasks.value
        val doneCount = tasks.count { it.task.isCompleted }
        val totalCount = tasks.size
        val rate = if (totalCount > 0) (doneCount * 100) / totalCount else 100

        viewModelScope.launch {
            repository.insertReview(
                Review(
                    reviewType = "DAILY",
                    date = todayStr,
                    completedTasksCount = doneCount,
                    totalTasksCount = totalCount,
                    completionRatePercent = rate,
                    reflectionWell = dailyReviewReflection.value.trim()
                )
            )
            showDailyReviewModal.value = false
            dailyReviewReflection.value = ""
            performHapticFeedback()
        }
    }

    // Weekly Review
    fun submitWeeklyReview() {
        val todayStr = DateTimeUtils.todayStr()
        val allT = allTasks.value
        val doneCount = allT.count { it.task.isCompleted }
        val rate = if (allT.isNotEmpty()) (doneCount * 100) / allT.size else 100
        val priorities = listOf(weeklyPriority1.value, weeklyPriority2.value, weeklyPriority3.value)
            .filter { it.isNotBlank() }

        viewModelScope.launch {
            repository.insertReview(
                Review(
                    reviewType = "WEEKLY",
                    date = todayStr,
                    completedTasksCount = doneCount,
                    totalTasksCount = allT.size,
                    completionRatePercent = rate,
                    reflectionWell = weeklyReviewWell.value.trim(),
                    reflectionImprove = weeklyReviewImprove.value.trim(),
                    nextWeekPriorities = priorities.joinToString(" | ")
                )
            )

            // Auto-create next week priority tasks!
            val nextWeekDate = LocalDate.now().plusDays(7).toString()
            priorities.forEach { pTitle ->
                repository.insertTask(
                    Task(
                        title = pTitle.trim(),
                        dueDate = nextWeekDate,
                        priority = Priority.HIGH,
                        categoryId = "work"
                    )
                )
            }

            showWeeklyReviewModal.value = false
            weeklyReviewStep.value = 1
            weeklyReviewWell.value = ""
            weeklyReviewImprove.value = ""
            weeklyPriority1.value = ""
            weeklyPriority2.value = ""
            weeklyPriority3.value = ""
            performHapticFeedback()
        }
    }
}
