package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_logs ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryLog>>

    @Query("SELECT * FROM history_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getHistorySince(sinceTimestamp: Long): Flow<List<HistoryLog>>

    @Query("SELECT * FROM history_logs WHERE garmentId = :garmentId AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getHistoryForGarment(garmentId: Long, sinceTimestamp: Long): Flow<List<HistoryLog>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: HistoryLog)
    
    @Query("DELETE FROM history_logs")
    suspend fun clearHistory()
}
