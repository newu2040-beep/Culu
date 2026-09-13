package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_REMINDERS = "culu_reminders_channel"
    const val CHANNEL_WATER = "culu_water_channel"

    const val NOTIFICATION_ID_WATER = 1001
    const val NOTIFICATION_ID_BASE_REMINDER = 2000

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "CULU Routine & Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily wellness, medicine and custom routine alerts"
                enableVibration(true)
                setShowBadge(true)
            }

            val waterChannel = NotificationChannel(
                CHANNEL_WATER,
                "CULU Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic water intake alerts throughout the day"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(waterChannel)
        }
    }

    fun showReminderNotification(
        context: Context,
        id: Int,
        title: String,
        message: String,
        isWater: Boolean = false
    ) {
        val channelId = if (isWater) CHANNEL_WATER else CHANNEL_REMINDERS
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }
}
