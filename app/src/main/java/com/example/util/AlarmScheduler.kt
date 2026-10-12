package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.Task
import com.example.receiver.TaskAlarmReceiver
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AlarmScheduler {

    /**
     * Standard offsets used across the app. Used when cancelling alarms.
     */
    private val KNOWN_OFFSETS = listOf(-1, 0, 5, 10, 15, 30, 45, 60, 120, 1440)

    /**
     * Generate unique deterministic pending intent request code per task and offset.
     */
    fun getRequestCode(taskId: Long, offsetMin: Int): Int {
        // Safe unique int hash for AlarmManager pending intents
        val normalizedOffset = (offsetMin + 1).coerceAtLeast(0) % 999
        return ((taskId % 100000L) * 1000L + normalizedOffset).toInt()
    }

    /**
     * Schedules notification(s) for a task.
     * Supports multiple notification timings for events/tasks:
     * - Before event (e.g. 15 mins, 30 mins, 1 hour before)
     * - At the time of event (0 mins)
     * - Fallback: tasks without specific time/offsets receive a 7:00 AM morning notification on task day.
     */
    fun scheduleTaskAlarm(context: Context, task: Task) {
        // Cancel existing pending notifications for this task first
        cancelTaskAlarm(context, task.id)

        if (task.isCompleted) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        try {
            val date = DateTimeUtils.parseDate(task.dueDate)
            val offsets = task.getReminderOffsetList()

            if (!task.dueTime.isNullOrBlank()) {
                val parts = task.dueTime.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val dueTime = LocalTime.of(h, m)
                val baseDateTime = LocalDateTime.of(date, dueTime)

                if (offsets.isNotEmpty()) {
                    // Schedule notification for each configured timing (e.g. 15m before, at event time)
                    for (offset in offsets) {
                        val triggerDateTime = baseDateTime.minusMinutes(offset.toLong())
                        scheduleSingleAlarm(
                            context = context,
                            alarmManager = alarmManager,
                            task = task,
                            triggerDateTime = triggerDateTime,
                            offsetMin = offset
                        )
                    }
                } else {
                    // Time set but no specific custom offset chosen -> Rule: notify at 7:00 AM on task day
                    val morningTrigger = LocalDateTime.of(date, LocalTime.of(7, 0))
                    scheduleSingleAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        task = task,
                        triggerDateTime = morningTrigger,
                        offsetMin = -1
                    )
                }
            } else {
                // Untimed task -> Rule: send notification at 7:00 AM of that day of the task
                val morningTrigger = LocalDateTime.of(date, LocalTime.of(7, 0))
                scheduleSingleAlarm(
                    context = context,
                    alarmManager = alarmManager,
                    task = task,
                    triggerDateTime = morningTrigger,
                    offsetMin = -1
                )
            }
        } catch (_: Exception) {}
    }

    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        task: Task,
        triggerDateTime: LocalDateTime,
        offsetMin: Int
    ) {
        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val nowMillis = System.currentTimeMillis()

        if (triggerMillis <= nowMillis) {
            // Already in the past
            return
        }

        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_TASK_REMINDER
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_NOTES, task.notes)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_CATEGORY, task.categoryId)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_PRIORITY, task.priority.name)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_DUE_TIME, task.dueTime)
            putExtra(TaskAlarmReceiver.EXTRA_OFFSET_MIN, offsetMin)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_REPEAT_RULE, task.repeatRule.name)
        }

        val requestCode = getRequestCode(task.id, offsetMin)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    /**
     * Cancels all scheduled notification alarms for this task.
     */
    fun cancelTaskAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_TASK_REMINDER
        }

        for (offset in KNOWN_OFFSETS) {
            val requestCode = getRequestCode(taskId, offset)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    fun scheduleSnoozeAlarm(context: Context, taskId: Long, title: String, category: String, minutes: Int = 10) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)

        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_TASK_REMINDER
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, title)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_NOTES, "Snoozed notification")
            putExtra(TaskAlarmReceiver.EXTRA_TASK_CATEGORY, category)
            putExtra(TaskAlarmReceiver.EXTRA_OFFSET_MIN, 0)
        }

        val requestCode = getRequestCode(taskId, 999)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
    }
}
