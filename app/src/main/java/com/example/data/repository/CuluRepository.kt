package com.example.data.repository

import com.example.data.database.ReminderDao
import com.example.data.database.ReminderItem
import com.example.data.database.ReminderLog
import com.example.data.database.ReminderLogDao
import com.example.data.database.WaterDao
import com.example.data.database.WaterEntry
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CuluRepository(
    private val waterDao: WaterDao,
    private val reminderDao: ReminderDao,
    private val reminderLogDao: ReminderLogDao,
    private val preferencesRepository: UserPreferencesRepository
) {
    val preferencesFlow: Flow<UserPreferences> = preferencesRepository.userPreferencesFlow

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getEntriesForDate(dateString: String): Flow<List<WaterEntry>> {
        return waterDao.getEntriesForDate(dateString)
    }

    fun getTotalIntakeForDate(dateString: String): Flow<Int?> {
        return waterDao.getTotalIntakeForDate(dateString)
    }

    fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<WaterEntry>> {
        return waterDao.getEntriesBetweenDates(startDate, endDate)
    }

    suspend fun addWaterEntry(amountMl: Int, note: String = ""): Long {
        val entry = WaterEntry(
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            dateString = getTodayDateString(),
            note = note
        )
        return waterDao.insertEntry(entry)
    }

    suspend fun removeWaterEntry(entry: WaterEntry) {
        waterDao.deleteEntry(entry)
    }

    suspend fun removeWaterEntryById(id: Long) {
        waterDao.deleteEntryById(id)
    }

    // Reminders
    val allReminders: Flow<List<ReminderItem>> = reminderDao.getAllReminders()
    val activeReminders: Flow<List<ReminderItem>> = reminderDao.getActiveReminders()

    fun getRemindersByType(type: String): Flow<List<ReminderItem>> = reminderDao.getRemindersByType(type)

    suspend fun insertReminder(reminder: ReminderItem): Long = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ReminderItem) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: ReminderItem) = reminderDao.deleteReminder(reminder)

    suspend fun toggleReminderActive(id: Long, isActive: Boolean) = reminderDao.toggleReminderActive(id, isActive)

    // Reminder Logs
    fun getLogsForDate(dateString: String): Flow<List<ReminderLog>> = reminderLogDao.getLogsForDate(dateString)

    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<ReminderLog>> =
        reminderLogDao.getLogsBetweenDates(startDate, endDate)

    suspend fun markReminderStatus(reminder: ReminderItem, status: String, dateString: String = getTodayDateString()) {
        val existing = reminderLogDao.getLogForReminderOnDate(reminder.id, dateString)
        if (existing != null) {
            reminderLogDao.updateLog(existing.copy(status = status, loggedAt = System.currentTimeMillis()))
        } else {
            val log = ReminderLog(
                reminderId = reminder.id,
                reminderTitle = reminder.title,
                reminderType = reminder.type,
                dateString = dateString,
                timeMinutes = reminder.timeMinutes,
                status = status,
                loggedAt = System.currentTimeMillis()
            )
            reminderLogDao.insertLog(log)
        }
    }

    // Preferences modifications
    suspend fun updateDailyGoal(goalMl: Int) = preferencesRepository.updateDailyWaterGoal(goalMl)
    suspend fun updateCustomQuickCup(cupMl: Int) = preferencesRepository.updateCustomQuickCup(cupMl)
    suspend fun updateWaterReminderInterval(minutes: Int) = preferencesRepository.updateWaterReminderInterval(minutes)
    suspend fun updateWaterRemindersEnabled(enabled: Boolean) = preferencesRepository.updateWaterRemindersEnabled(enabled)
    suspend fun updateThemeMode(mode: String) = preferencesRepository.updateThemeMode(mode)
    suspend fun updateCompactMode(enabled: Boolean) = preferencesRepository.updateCompactMode(enabled)
    suspend fun updateHapticsEnabled(enabled: Boolean) = preferencesRepository.updateHapticsEnabled(enabled)
    suspend fun updateAllNotificationsEnabled(enabled: Boolean) = preferencesRepository.updateAllNotificationsEnabled(enabled)
    suspend fun updateUserProfile(name: String, age: Int, gender: String, photoUri: String) = preferencesRepository.updateUserProfile(name, age, gender, photoUri)

    // Helper: calculate last 7 days date strings
    fun getLast7Days(): List<Pair<String, String>> {
        // returns list of (formatted "yyyy-MM-dd", dayLabel e.g. "Mon")
        val result = mutableListOf<Pair<String, String>>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.timeInMillis = cal.timeInMillis - (i * 24L * 60 * 60 * 1000)
            val dateStr = sdf.format(c.time)
            val label = dayFormat.format(c.time)
            result.add(Pair(dateStr, label))
        }
        return result
    }
}
