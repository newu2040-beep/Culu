package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.database.ReminderItem
import java.util.Calendar

object ReminderAlarmScheduler {

    fun scheduleReminder(context: Context, reminder: ReminderItem) {
        if (!reminder.isActive || !reminder.isNotificationEnabled) {
            cancelReminder(context, reminder.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = "com.example.culu.ACTION_REMINDER"
            putExtra(ReminderAlarmReceiver.EXTRA_ID, (NotificationHelper.NOTIFICATION_ID_BASE_REMINDER + reminder.id).toInt())
            putExtra(ReminderAlarmReceiver.EXTRA_TITLE, reminder.title)
            val subtext = if (reminder.dosage.isNotEmpty()) "Dosage: ${reminder.dosage}" else "Scheduled for ${reminder.formattedTime}"
            putExtra(ReminderAlarmReceiver.EXTRA_MESSAGE, subtext)
            putExtra(ReminderAlarmReceiver.EXTRA_DOSAGE, reminder.dosage)
            putExtra(ReminderAlarmReceiver.EXTRA_IS_WATER, reminder.type == ReminderItem.TYPE_WATER)
            putExtra(ReminderAlarmReceiver.EXTRA_SPEAK_LOUD, reminder.speakLoud)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (NotificationHelper.NOTIFICATION_ID_BASE_REMINDER + reminder.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            val hours = reminder.timeMinutes / 60
            val minutes = reminder.timeMinutes % 60

            if (reminder.targetDateMillis != null) {
                // Exact custom date reminder
                val targetCal = Calendar.getInstance().apply { timeInMillis = reminder.targetDateMillis }
                set(Calendar.YEAR, targetCal.get(Calendar.YEAR))
                set(Calendar.MONTH, targetCal.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, targetCal.get(Calendar.DAY_OF_MONTH))
            }

            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time is in the past for recurring reminder, schedule for tomorrow
            if (reminder.targetDateMillis == null && timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // If specific date is already in the past, do not schedule
        if (reminder.targetDateMillis != null && calendar.timeInMillis <= System.currentTimeMillis()) {
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Graceful fallback for restricted background alarms
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    /**
     * Immediately triggers a real-time notification with vibration and loud speech
     * so user can verify real-time alerts right away.
     */
    fun triggerImmediateTestAlert(
        context: Context,
        title: String = "Hydration & Vitamin D",
        dosage: String = "Take 1 Tablet with 250ml water",
        speakLoud: Boolean = true
    ) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = "com.example.culu.ACTION_REMINDER"
            putExtra(ReminderAlarmReceiver.EXTRA_ID, 9999)
            putExtra(ReminderAlarmReceiver.EXTRA_TITLE, title)
            putExtra(ReminderAlarmReceiver.EXTRA_MESSAGE, if (dosage.isNotEmpty()) "Dosage: $dosage" else "Scheduled routine")
            putExtra(ReminderAlarmReceiver.EXTRA_DOSAGE, dosage)
            putExtra(ReminderAlarmReceiver.EXTRA_IS_WATER, false)
            putExtra(ReminderAlarmReceiver.EXTRA_SPEAK_LOUD, speakLoud)
        }
        context.sendBroadcast(intent)
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = "com.example.culu.ACTION_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (NotificationHelper.NOTIFICATION_ID_BASE_REMINDER + reminderId).toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleWaterInterval(context: Context, intervalMinutes: Int, enabled: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = "com.example.culu.ACTION_REMINDER"
            putExtra(ReminderAlarmReceiver.EXTRA_ID, NotificationHelper.NOTIFICATION_ID_WATER)
            putExtra(ReminderAlarmReceiver.EXTRA_TITLE, "Stay Hydrated • CULU")
            putExtra(ReminderAlarmReceiver.EXTRA_MESSAGE, "Time for a refreshing sip of water to hit your daily goal!")
            putExtra(ReminderAlarmReceiver.EXTRA_IS_WATER, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NotificationHelper.NOTIFICATION_ID_WATER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled || intervalMinutes <= 0) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            return
        }

        val triggerTime = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L)
        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                intervalMinutes * 60 * 1000L,
                pendingIntent
            )
        } catch (_: Exception) {
            // fallback
        }
    }

    fun rescheduleAll(context: Context) {
        // Will be called on boot or manual sync
    }
}
