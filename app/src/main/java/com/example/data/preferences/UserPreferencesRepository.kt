package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "culu_preferences")

data class UserPreferences(
    val dailyWaterGoalMl: Int = 2500,
    val customQuickCupMl: Int = 350,
    val waterReminderIntervalMinutes: Int = 90,
    val waterRemindersEnabled: Boolean = true,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val compactModeEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val allNotificationsEnabled: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DAILY_WATER_GOAL = intPreferencesKey("daily_water_goal_ml")
        val CUSTOM_QUICK_CUP = intPreferencesKey("custom_quick_cup_ml")
        val WATER_REMINDER_INTERVAL = intPreferencesKey("water_reminder_interval")
        val WATER_REMINDERS_ENABLED = booleanPreferencesKey("water_reminders_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COMPACT_MODE = booleanPreferencesKey("compact_mode")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ALL_NOTIFICATIONS_ENABLED = booleanPreferencesKey("all_notifications_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                dailyWaterGoalMl = preferences[PreferencesKeys.DAILY_WATER_GOAL] ?: 2500,
                customQuickCupMl = preferences[PreferencesKeys.CUSTOM_QUICK_CUP] ?: 350,
                waterReminderIntervalMinutes = preferences[PreferencesKeys.WATER_REMINDER_INTERVAL] ?: 90,
                waterRemindersEnabled = preferences[PreferencesKeys.WATER_REMINDERS_ENABLED] ?: true,
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
                compactModeEnabled = preferences[PreferencesKeys.COMPACT_MODE] ?: false,
                hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true,
                allNotificationsEnabled = preferences[PreferencesKeys.ALL_NOTIFICATIONS_ENABLED] ?: true
            )
        }

    suspend fun updateDailyWaterGoal(goalMl: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_WATER_GOAL] = goalMl
        }
    }

    suspend fun updateCustomQuickCup(cupMl: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_QUICK_CUP] = cupMl
        }
    }

    suspend fun updateWaterReminderInterval(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATER_REMINDER_INTERVAL] = minutes
        }
    }

    suspend fun updateWaterRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATER_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun updateCompactMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPACT_MODE] = enabled
        }
    }

    suspend fun updateHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun updateAllNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALL_NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
