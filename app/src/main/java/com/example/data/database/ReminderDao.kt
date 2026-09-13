package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder_items ORDER BY timeMinutes ASC")
    fun getAllReminders(): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminder_items WHERE isActive = 1 ORDER BY timeMinutes ASC")
    fun getActiveReminders(): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminder_items WHERE type = :type ORDER BY timeMinutes ASC")
    fun getRemindersByType(type: String): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminder_items WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderItem): Long

    @Update
    suspend fun updateReminder(reminder: ReminderItem)

    @Delete
    suspend fun deleteReminder(reminder: ReminderItem)

    @Query("DELETE FROM reminder_items WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("UPDATE reminder_items SET isActive = :isActive WHERE id = :id")
    suspend fun toggleReminderActive(id: Long, isActive: Boolean)
}
