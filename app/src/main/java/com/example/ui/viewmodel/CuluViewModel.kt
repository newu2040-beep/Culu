package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.CuluApplication
import com.example.data.database.ReminderItem
import com.example.data.database.ReminderLog
import com.example.data.database.WaterEntry
import com.example.data.preferences.UserPreferences
import com.example.data.repository.CuluRepository
import com.example.notifications.ReminderAlarmScheduler
import com.example.utils.HapticUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyWaterStat(
    val dateString: String,
    val dayLabel: String,
    val intakeMl: Int,
    val goalMl: Int,
    val percentage: Float
)

data class ReminderStats(
    val completedCount: Int,
    val skippedCount: Int,
    val pendingCount: Int,
    val totalCount: Int,
    val completionPercentage: Float
)

class CuluViewModel(
    application: Application,
    private val repository: CuluRepository
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // Navigation and Modal UI states
    private val _currentTab = MutableStateFlow(0) // 0=Home, 1=Water, 2=Reminders, 3=History
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isAddReminderOpen = MutableStateFlow(false)
    val isAddReminderOpen: StateFlow<Boolean> = _isAddReminderOpen.asStateFlow()

    private val _editingReminder = MutableStateFlow<ReminderItem?>(null)
    val editingReminder: StateFlow<ReminderItem?> = _editingReminder.asStateFlow()

    // Reaction trigger for visual water flare
    private val _waterFlareActive = MutableStateFlow(false)
    val waterFlareActive: StateFlow<Boolean> = _waterFlareActive.asStateFlow()

    // User Preferences
    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val todayDateString: String = repository.getTodayDateString()

    // Today's water entries & total
    val todayWaterEntries: StateFlow<List<WaterEntry>> = repository.getEntriesForDate(todayDateString)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayWaterTotal: StateFlow<Int> = repository.getTotalIntakeForDate(todayDateString)
        .combine(preferences) { total, _ -> total ?: 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Reminders
    val allReminders: StateFlow<List<ReminderItem>> = repository.allReminders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Today's logs
    val todayLogs: StateFlow<List<ReminderLog>> = repository.getLogsForDate(todayDateString)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 7-Day History Stats
    val past7DaysStats: StateFlow<List<DailyWaterStat>> = combine(
        repository.preferencesFlow,
        repository.getEntriesBetweenDates(
            repository.getLast7Days().first().first,
            repository.getLast7Days().last().first
        )
    ) { prefs, entries ->
        val last7Days = repository.getLast7Days()
        val entriesByDate = entries.groupBy { it.dateString }
        last7Days.map { (dateStr, dayLabel) ->
            val intake = entriesByDate[dateStr]?.sumOf { it.amountMl } ?: 0
            val goal = prefs.dailyWaterGoalMl
            val pct = if (goal > 0) (intake.toFloat() / goal.toFloat()).coerceIn(0f, 1.5f) else 0f
            DailyWaterStat(
                dateString = dateStr,
                dayLabel = dayLabel,
                intakeMl = intake,
                goalMl = goal,
                percentage = pct
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reminder Completion Stats
    val reminderStats: StateFlow<ReminderStats> = combine(
        allReminders,
        todayLogs
    ) { reminders, logs ->
        val active = reminders.filter { it.isActive }
        val logMap = logs.associateBy { it.reminderId }
        var completed = 0
        var skipped = 0
        var pending = 0

        active.forEach { reminder ->
            val log = logMap[reminder.id]
            when (log?.status) {
                ReminderLog.STATUS_TAKEN -> completed++
                ReminderLog.STATUS_SKIPPED -> skipped++
                else -> pending++
            }
        }

        val total = active.size
        val pct = if (total > 0) completed.toFloat() / total.toFloat() else 1f
        ReminderStats(
            completedCount = completed,
            skippedCount = skipped,
            pendingCount = pending,
            totalCount = total,
            completionPercentage = pct
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReminderStats(0, 0, 0, 0, 0f)
    )

    // UI Navigation
    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun openSettings(open: Boolean) {
        _isSettingsOpen.value = open
    }

    fun openAddReminder(open: Boolean) {
        if (!open) {
            _editingReminder.value = null
        }
        _isAddReminderOpen.value = open
    }

    fun openEditReminder(reminder: ReminderItem) {
        _editingReminder.value = reminder
        _isAddReminderOpen.value = true
    }

    fun closeAddOrEditReminder() {
        _editingReminder.value = null
        _isAddReminderOpen.value = false
    }

    // Water Operations
    fun addWater(amountMl: Int, note: String = "") {
        viewModelScope.launch {
            repository.addWaterEntry(amountMl, note)
            HapticUtils.performWaterTick(context, preferences.value.hapticsEnabled)

            // Trigger temporary glass specular flare
            _waterFlareActive.value = true
            kotlinx.coroutines.delay(650)
            _waterFlareActive.value = false
        }
    }

    fun removeWaterEntry(entry: WaterEntry) {
        viewModelScope.launch {
            repository.removeWaterEntry(entry)
            HapticUtils.performWaterTick(context, preferences.value.hapticsEnabled)
        }
    }

    fun removeWaterEntryById(id: Long) {
        viewModelScope.launch {
            repository.removeWaterEntryById(id)
            HapticUtils.performWaterTick(context, preferences.value.hapticsEnabled)
        }
    }

    // Reminder Operations
    fun addReminder(
        title: String,
        type: String,
        dosage: String,
        timeMinutes: Int,
        daysOfWeek: String,
        isNotificationEnabled: Boolean,
        iconName: String,
        categoryName: String,
        targetDateMillis: Long? = null,
        speakLoud: Boolean = true
    ) {
        viewModelScope.launch {
            val reminder = ReminderItem(
                title = title.trim(),
                type = type,
                dosage = dosage.trim(),
                timeMinutes = timeMinutes,
                daysOfWeek = daysOfWeek,
                isNotificationEnabled = isNotificationEnabled,
                isActive = true,
                iconName = iconName,
                categoryName = categoryName,
                targetDateMillis = targetDateMillis,
                speakLoud = speakLoud
            )
            val id = repository.insertReminder(reminder)
            if (isNotificationEnabled) {
                ReminderAlarmScheduler.scheduleReminder(context, reminder.copy(id = id))
            }
            HapticUtils.performSuccess(context, preferences.value.hapticsEnabled)
            _editingReminder.value = null
            _isAddReminderOpen.value = false
        }
    }

    fun updateReminder(reminder: ReminderItem) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            if (reminder.isActive && reminder.isNotificationEnabled) {
                ReminderAlarmScheduler.scheduleReminder(context, reminder)
            } else {
                ReminderAlarmScheduler.cancelReminder(context, reminder.id)
            }
            HapticUtils.performSuccess(context, preferences.value.hapticsEnabled)
            _editingReminder.value = null
            _isAddReminderOpen.value = false
        }
    }

    fun testRealTimeAlert(
        title: String = "Hydration & Vitamin D",
        dosage: String = "Take 1 Tablet with 250ml water",
        speakLoud: Boolean = true
    ) {
        ReminderAlarmScheduler.triggerImmediateTestAlert(
            context = context,
            title = title,
            dosage = dosage,
            speakLoud = speakLoud
        )
        HapticUtils.performSuccess(context, preferences.value.hapticsEnabled)
    }

    fun speakReminderAloud(reminder: ReminderItem) {
        com.example.utils.TtsSpeaker.speakReminder(context, reminder.title, reminder.dosage)
        HapticUtils.performClick(null, preferences.value.hapticsEnabled)
    }

    fun speakLoudText(text: String) {
        com.example.utils.TtsSpeaker.speakLoud(context, text)
        HapticUtils.performClick(null, preferences.value.hapticsEnabled)
    }

    fun toggleReminderActive(reminder: ReminderItem) {
        viewModelScope.launch {
            val newStatus = !reminder.isActive
            repository.toggleReminderActive(reminder.id, newStatus)
            if (newStatus && reminder.isNotificationEnabled) {
                ReminderAlarmScheduler.scheduleReminder(context, reminder.copy(isActive = true))
            } else {
                ReminderAlarmScheduler.cancelReminder(context, reminder.id)
            }
            HapticUtils.performClick(null, preferences.value.hapticsEnabled)
        }
    }

    fun deleteReminder(reminder: ReminderItem) {
        viewModelScope.launch {
            ReminderAlarmScheduler.cancelReminder(context, reminder.id)
            repository.deleteReminder(reminder)
            HapticUtils.performClick(null, preferences.value.hapticsEnabled)
        }
    }

    fun markReminderStatus(reminder: ReminderItem, status: String) {
        viewModelScope.launch {
            repository.markReminderStatus(reminder, status, todayDateString)
            if (status == ReminderLog.STATUS_TAKEN) {
                HapticUtils.performSuccess(context, preferences.value.hapticsEnabled)
            } else {
                HapticUtils.performClick(null, preferences.value.hapticsEnabled)
            }
        }
    }

    // Preferences modifications
    fun updateDailyWaterGoal(goalMl: Int) {
        viewModelScope.launch {
            repository.updateDailyGoal(goalMl)
        }
    }

    fun updateCustomQuickCup(cupMl: Int) {
        viewModelScope.launch {
            repository.updateCustomQuickCup(cupMl)
        }
    }

    fun updateWaterReminderInterval(minutes: Int) {
        viewModelScope.launch {
            repository.updateWaterReminderInterval(minutes)
            ReminderAlarmScheduler.scheduleWaterInterval(
                context,
                minutes,
                preferences.value.waterRemindersEnabled
            )
        }
    }

    fun updateWaterRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateWaterRemindersEnabled(enabled)
            ReminderAlarmScheduler.scheduleWaterInterval(
                context,
                preferences.value.waterReminderIntervalMinutes,
                enabled
            )
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun updateUserProfile(name: String, age: Int, gender: String, photoUri: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name, age, gender, photoUri)
            HapticUtils.performSuccess(context, preferences.value.hapticsEnabled)
        }
    }

    fun updateCompactMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateCompactMode(enabled)
        }
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateHapticsEnabled(enabled)
        }
    }

    fun updateAllNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAllNotificationsEnabled(enabled)
        }
    }

    companion object {
        fun provideFactory(application: CuluApplication): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CuluViewModel(application, application.repository) as T
                }
            }
    }
}
