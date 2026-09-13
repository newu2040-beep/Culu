package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.utils.TtsSpeaker

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Restore alarms on device boot
            ReminderAlarmScheduler.rescheduleAll(context)
            return
        }

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "CULU Reminder"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Time for your scheduled routine"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val id = intent.getIntExtra(EXTRA_ID, 1)
        val isWater = intent.getBooleanExtra(EXTRA_IS_WATER, false)
        val speakLoud = intent.getBooleanExtra(EXTRA_SPEAK_LOUD, true)

        NotificationHelper.createNotificationChannels(context)
        NotificationHelper.showReminderNotification(
            context = context,
            id = id,
            title = title,
            message = message,
            isWater = isWater
        )

        if (speakLoud) {
            TtsSpeaker.speakReminder(context, title, dosage)
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_reminder_title"
        const val EXTRA_MESSAGE = "extra_reminder_message"
        const val EXTRA_DOSAGE = "extra_reminder_dosage"
        const val EXTRA_ID = "extra_reminder_id"
        const val EXTRA_IS_WATER = "extra_is_water"
        const val EXTRA_SPEAK_LOUD = "extra_speak_loud"
    }
}
