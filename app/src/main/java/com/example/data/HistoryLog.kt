package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

// Bug 3: @Immutable ensures Compose skips recomposition when history logs are rendered in lists.
@Immutable
@Entity(tableName = "history_logs")
data class HistoryLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val garmentId: Long? = null,
    val size: String? = null,
    val stockValue: Int? = null,
    val totalStockValue: Int? = null,
    val actionType: String = "UPDATE"
)
