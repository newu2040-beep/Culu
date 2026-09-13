package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_logs")
data class ReminderLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reminderId: Long,
    val reminderTitle: String,
    val reminderType: String,
    val dateString: String, // "YYYY-MM-DD"
    val timeMinutes: Int,
    val status: String, // "TAKEN", "SKIPPED", "PENDING"
    val loggedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_TAKEN = "TAKEN"
        const val STATUS_SKIPPED = "SKIPPED"
        const val STATUS_PENDING = "PENDING"
    }
}
