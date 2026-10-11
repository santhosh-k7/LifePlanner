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
     * Schedules a notification for a task.
     * Per user requirement:
     * - "change notification & alarm into only notifications"
     * - "Generally send all the tasks which notifications are not scheduled by 7:00 am of that day of the task"
     */
    fun scheduleTaskAlarm(context: Context, task: Task) {
        if (task.isCompleted) {
            cancelTaskAlarm(context, task.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        try {
            val date = DateTimeUtils.parseDate(task.dueDate)
            val triggerDateTime: LocalDateTime

            if (task.reminderOffsetMin != null && !task.dueTime.isNullOrBlank()) {
                // Specific time + specific reminder offset (e.g. at time, 10m before, 30m before, 1h before)
                val parts = task.dueTime.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val dueTime = LocalTime.of(h, m)
                val baseDateTime = LocalDateTime.of(date, dueTime)
                triggerDateTime = baseDateTime.minusMinutes(task.reminderOffsetMin.toLong())
            } else if (!task.dueTime.isNullOrBlank() && task.reminderOffsetMin == null) {
                // Time set without custom offset -> user rule: notify at 7:00 AM on task day
                triggerDateTime = LocalDateTime.of(date, LocalTime.of(7, 0))
            } else {
                // No specific notification or time scheduled -> Rule: send all tasks by 7:00 am of that day of the task
                triggerDateTime = LocalDateTime.of(date, LocalTime.of(7, 0))
            }

            val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val nowMillis = System.currentTimeMillis()

            if (triggerMillis <= nowMillis) {
                // Time has already passed for this notification
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
                putExtra(TaskAlarmReceiver.EXTRA_TASK_MEDIUM, task.medium)
                putExtra(TaskAlarmReceiver.EXTRA_TASK_REPEAT_RULE, task.repeatRule.name)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.toInt(),
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
        } catch (_: Exception) {}
    }

    fun cancelTaskAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_TASK_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
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
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
    }
}
