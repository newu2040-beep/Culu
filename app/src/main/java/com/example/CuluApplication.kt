package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.CuluRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class CuluApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val preferencesRepository by lazy { UserPreferencesRepository(this) }
    val repository by lazy {
        CuluRepository(
            waterDao = database.waterDao(),
            reminderDao = database.reminderDao(),
            reminderLogDao = database.reminderLogDao(),
            preferencesRepository = preferencesRepository
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
