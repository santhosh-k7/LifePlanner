package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.Priority
import com.example.receiver.TaskAlarmReceiver

object NotificationHelper {

    const val CHANNEL_TASK_REMINDERS = "lifeplanner_task_reminders"
    const val CHANNEL_DAILY_REVIEW = "lifeplanner_daily_review"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_TASK_REMINDERS,
                "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications and timely reminders for scheduled tasks"
                enableVibration(true)
                enableLights(true)
            }

            val reviewChannel = NotificationChannel(
                CHANNEL_DAILY_REVIEW,
                "Daily & Weekly Reflections",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle evening review reminders"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(reviewChannel)
        }
    }

    fun showTaskNotification(
        context: Context,
        taskId: Long,
        title: String,
        notes: String,
        category: String,
        priority: Priority,
        dueTime: String?,
        medium: String? = null
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels(context)

        // Intent to open app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_TASK_ID", taskId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Done" action
        val doneIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_MARK_DONE
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 10 + 1).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Snooze 10m" action
        val snoozeIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_SNOOZE_10M
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, title)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_CATEGORY, category)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val contentText = buildString {
            if (!dueTime.isNullOrBlank()) {
                append("Time: ${DateTimeUtils.formatTime12H(dueTime)} · ")
            }
            if (!medium.isNullOrBlank()) {
                append("$medium · ")
            }
            append("#${category.replaceFirstChar { it.uppercase() }}")
            if (notes.isNotBlank()) {
                append(" · $notes")
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔔 $title")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_agenda, "Mark Done", donePendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 10m", snoozePendingIntent)
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }

    fun cancelNotification(context: Context, taskId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskId.toInt())
    }

    fun triggerFocusTimerCompletedAlert(context: Context, taskTitle: String) {
        createNotificationChannels(context)

        // 1. Play audible notification sound immediately
        try {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context, soundUri)
            ringtone?.play()
        } catch (_: Exception) {}

        // 2. Strong 3-burst haptic vibration pattern
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                val vibrationEffect = android.os.VibrationEffect.createWaveform(
                    longArrayOf(0, 350, 150, 350, 150, 500),
                    intArrayOf(0, 255, 0, 255, 0, 255),
                    -1
                )
                vibratorManager?.defaultVibrator?.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        android.os.VibrationEffect.createWaveform(
                            longArrayOf(0, 350, 150, 350, 150, 500),
                            -1
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 350, 150, 350, 150, 500), -1)
                }
            }
        } catch (_: Exception) {}

        // 3. Post System Notification for heads-up alert
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                9999,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🎯 Focus Session Finished!")
                .setContentText("Completed: $taskTitle. Great work staying focused!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 350, 150, 350, 150, 500))
                .setContentIntent(openAppPendingIntent)
                .build()

            notificationManager.notify(9999, notification)
        } catch (_: Exception) {}
    }
}
