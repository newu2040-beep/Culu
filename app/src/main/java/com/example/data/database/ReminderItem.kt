package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_items")
data class ReminderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String = TYPE_CUSTOM, // "MEDICINE", "WATER", "CUSTOM"
    val dosage: String = "",
    val timeMinutes: Int = 540, // 9:00 AM default
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Mon .. 7=Sun
    val isNotificationEnabled: Boolean = true,
    val isActive: Boolean = true,
    val iconName: String = "routine",
    val categoryName: String = "General",
    val targetDateMillis: Long? = null, // Specific date if not recurring
    val speakLoud: Boolean = true, // Speak aloud using Text-to-Speech
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_MEDICINE = "MEDICINE"
        const val TYPE_WATER = "WATER"
        const val TYPE_CUSTOM = "CUSTOM"
    }

    fun isScheduledForDay(dayOfWeekIso: Int): Boolean {
        // If it's a specific date reminder, check if today is that date
        if (targetDateMillis != null) {
            val targetCal = java.util.Calendar.getInstance().apply { timeInMillis = targetDateMillis }
            val nowCal = java.util.Calendar.getInstance()
            return targetCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
                   targetCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)
        }
        // 1 = Monday, 7 = Sunday
        val days = daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
        return days.contains(dayOfWeekIso)
    }

    val formattedSchedule: String
        get() {
            if (targetDateMillis != null) {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = targetDateMillis }
                val now = java.util.Calendar.getInstance()
                val isToday = cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                              cal.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)
                now.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val isTomorrow = cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                                 cal.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)

                return when {
                    isToday -> "Today ($formattedTime)"
                    isTomorrow -> "Tomorrow ($formattedTime)"
                    else -> {
                        val sdf = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                        "${sdf.format(java.util.Date(targetDateMillis))} at $formattedTime"
                    }
                }
            }

            val count = daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.size
            return if (count == 7) "Every day at $formattedTime" else "$count days/week at $formattedTime"
        }

    val formattedTime: String
        get() {
            val hours = timeMinutes / 60
            val minutes = timeMinutes % 60
            val isPm = hours >= 12
            val displayHour = when {
                hours == 0 -> 12
                hours > 12 -> hours - 12
                else -> hours
            }
            return "%d:%02d %s".format(displayHour, minutes, if (isPm) "PM" else "AM")
        }
}
