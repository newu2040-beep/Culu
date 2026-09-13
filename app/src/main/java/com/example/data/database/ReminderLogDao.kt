package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderLogDao {
    @Query("SELECT * FROM reminder_logs WHERE dateString = :dateString ORDER BY loggedAt DESC")
    fun getLogsForDate(dateString: String): Flow<List<ReminderLog>>

    @Query("SELECT * FROM reminder_logs WHERE dateString BETWEEN :startDate AND :endDate ORDER BY loggedAt DESC")
    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<ReminderLog>>

    @Query("SELECT * FROM reminder_logs WHERE reminderId = :reminderId AND dateString = :dateString LIMIT 1")
    suspend fun getLogForReminderOnDate(reminderId: Long, dateString: String): ReminderLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReminderLog): Long

    @Update
    suspend fun updateLog(log: ReminderLog)

    @Query("DELETE FROM reminder_logs WHERE reminderId = :reminderId AND dateString = :dateString")
    suspend fun deleteLogForReminderOnDate(reminderId: Long, dateString: String)
}
