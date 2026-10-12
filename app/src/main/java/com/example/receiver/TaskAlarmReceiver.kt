package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.LifePlannerDatabase
import com.example.data.model.Priority
import com.example.data.model.RepeatRule
import com.example.data.model.Task
import com.example.util.AlarmScheduler
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TASK_REMINDER = "com.example.lifeplanner.ACTION_TASK_REMINDER"
        const val ACTION_MARK_DONE = "com.example.lifeplanner.ACTION_MARK_DONE"
        const val ACTION_SNOOZE_10M = "com.example.lifeplanner.ACTION_SNOOZE_10M"

        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
        const val EXTRA_TASK_NOTES = "EXTRA_TASK_NOTES"
        const val EXTRA_TASK_CATEGORY = "EXTRA_TASK_CATEGORY"
        const val EXTRA_TASK_PRIORITY = "EXTRA_TASK_PRIORITY"
        const val EXTRA_TASK_DUE_TIME = "EXTRA_TASK_DUE_TIME"
        const val EXTRA_OFFSET_MIN = "EXTRA_OFFSET_MIN"
        const val EXTRA_TASK_REPEAT_RULE = "EXTRA_TASK_REPEAT_RULE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)

        when (action) {
            ACTION_TASK_REMINDER -> {
                val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
                val notes = intent.getStringExtra(EXTRA_TASK_NOTES) ?: ""
                val category = intent.getStringExtra(EXTRA_TASK_CATEGORY) ?: "other"
                val priorityStr = intent.getStringExtra(EXTRA_TASK_PRIORITY) ?: "MEDIUM"
                val dueTime = intent.getStringExtra(EXTRA_TASK_DUE_TIME)
                val offsetMin = intent.getIntExtra(EXTRA_OFFSET_MIN, -1)
                val repeatRuleStr = intent.getStringExtra(EXTRA_TASK_REPEAT_RULE) ?: "NONE"

                val priority = try { Priority.valueOf(priorityStr) } catch (_: Exception) { Priority.MEDIUM }
                val repeatRule = try { RepeatRule.valueOf(repeatRuleStr) } catch (_: Exception) { RepeatRule.NONE }

                NotificationHelper.showTaskNotification(
                    context = context,
                    taskId = taskId,
                    title = title,
                    notes = notes,
                    category = category,
                    priority = priority,
                    dueTime = dueTime,
                    offsetMin = offsetMin
                )

                // If repeated according to user, schedule next cycle alarm
                if (repeatRule != RepeatRule.NONE) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = LifePlannerDatabase.getDatabase(context)
                            val taskWithSub = db.taskDao().getTaskById(taskId).firstOrNull()
                            if (taskWithSub != null && !taskWithSub.task.isCompleted) {
                                val currentDue = com.example.util.DateTimeUtils.parseDate(taskWithSub.task.dueDate)
                                val nextDue = when (repeatRule) {
                                    RepeatRule.DAILY -> currentDue.plusDays(1)
                                    RepeatRule.WEEKLY -> currentDue.plusWeeks(1)
                                    RepeatRule.MONTHLY -> currentDue.plusMonths(1)
                                    RepeatRule.NONE -> null
                                }
                                if (nextDue != null) {
                                    val updated = taskWithSub.task.copy(dueDate = com.example.util.DateTimeUtils.formatDate(nextDue))
                                    db.taskDao().updateTask(updated)
                                    AlarmScheduler.scheduleTaskAlarm(context, updated)
                                }
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            ACTION_MARK_DONE -> {
                NotificationHelper.cancelNotification(context, taskId)
                if (taskId > 0) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = LifePlannerDatabase.getDatabase(context)
                            db.taskDao().setTaskCompleted(taskId, true, System.currentTimeMillis())
                            AlarmScheduler.cancelTaskAlarm(context, taskId)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            ACTION_SNOOZE_10M -> {
                NotificationHelper.cancelNotification(context, taskId)
                val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
                val category = intent.getStringExtra(EXTRA_TASK_CATEGORY) ?: "other"
                AlarmScheduler.scheduleSnoozeAlarm(context, taskId, title, category, minutes = 10)
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = LifePlannerDatabase.getDatabase(context)
                        val allTasks = db.taskDao().getAllTasks().firstOrNull() ?: emptyList()
                        allTasks.forEach { item ->
                            if (!item.task.isCompleted) {
                                AlarmScheduler.scheduleTaskAlarm(context, item.task)
                            }
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
