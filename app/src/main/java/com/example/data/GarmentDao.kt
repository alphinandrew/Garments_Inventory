package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GarmentDao {
    @Query("SELECT * FROM garment_items WHERE isDeleted = 0 ORDER BY id ASC")
    fun getAllGarmentItems(): Flow<List<GarmentItem>>

    @Query("SELECT * FROM garment_items WHERE isDeleted = 0 ORDER BY id ASC")
    suspend fun getAllGarmentsList(): List<GarmentItem>

    @Query("SELECT * FROM garment_items ORDER BY id ASC")
    suspend fun getAllGarmentsIncludingDeleted(): List<GarmentItem>

    @Query("SELECT * FROM garment_items WHERE id = :id")
    suspend fun getGarmentItemById(id: Long): GarmentItem?

    @Query("SELECT * FROM garment_items WHERE isDeleted = 0 AND (categoryName LIKE '%' || :query || '%' OR subStyle LIKE '%' || :query || '%' OR sizeVariations LIKE '%' || :query || '%') ORDER BY id ASC")
    fun searchGarments(query: String): Flow<List<GarmentItem>>

    @Query("SELECT COUNT(*) FROM garment_items WHERE isDeleted = 0")
    suspend fun getItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGarment(item: GarmentItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<GarmentItem>)

    @Update
    suspend fun updateGarment(item: GarmentItem)

    @Delete
    suspend fun deleteGarment(item: GarmentItem)

    @Query("DELETE FROM garment_items WHERE id = :id")
    suspend fun deleteGarmentById(id: Long)

    @Query("DELETE FROM garment_items")
    suspend fun deleteAll()

    // Feature 2: Soft delete persistence to allow undo across process death
    @Query("UPDATE garment_items SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteGarment(id: Long, deletedAt: Long)

    @Query("UPDATE garment_items SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreGarment(id: Long)

    @Query("SELECT * FROM garment_items WHERE isDeleted = 1 ORDER BY deletedAt DESC LIMIT 1")
    suspend fun getMostRecentlyDeleted(): GarmentItem?

    @Query("SELECT * FROM garment_items WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedGarments(): Flow<List<GarmentItem>>

    @Query("DELETE FROM garment_items WHERE isDeleted = 1 AND deletedAt < :cutoffTimestamp")
    suspend fun purgeOldDeleted(cutoffTimestamp: Long)
}
