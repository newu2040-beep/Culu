package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_entries WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getEntriesForDate(dateString: String): Flow<List<WaterEntry>>

    @Query("SELECT SUM(amountMl) FROM water_entries WHERE dateString = :dateString")
    fun getTotalIntakeForDate(dateString: String): Flow<Int?>

    @Query("SELECT * FROM water_entries WHERE dateString BETWEEN :startDate AND :endDate ORDER BY timestamp ASC")
    fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<WaterEntry>>

    @Query("SELECT * FROM water_entries ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEntries(): Flow<List<WaterEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WaterEntry): Long

    @Delete
    suspend fun deleteEntry(entry: WaterEntry)

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM water_entries WHERE dateString = :dateString")
    suspend fun clearEntriesForDate(dateString: String)
}
