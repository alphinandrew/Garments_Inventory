package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

// Bug 3: Compose compiler marked GarmentItem unstable due to List/Map fields, causing full-list recompositions.
// @Immutable guarantees stability so Compose can skip recompositions on unchanged items.
// Feature 1 & 2: Soft-delete fields persist undo stack across process death; lowStockThreshold supports per-item alerts.
@Immutable
@Entity(tableName = "garment_items")
data class GarmentItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryName: String,
    val subStyle: String,
    val sizeVariations: List<String>,
    val sizeTypeCategory: String, // "Girls/Kids Numeric", "Standard Alpha (XS-XXL)", "Pant Waist (34-40)", "Maternity/Free Size"
    val handwrittenSizes: List<String> = emptyList(),
    val isPantModel: Boolean = false, // Critical flag to ensure Pant sizes are strictly not mixed
    val notes: String = "",
    val itemCode: String = "",
    val stockPerSize: Map<String, Int> = emptyMap(),
    val totalStock: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val lowStockThreshold: Int? = null
)
