package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class GarmentRepository(
    private val garmentDao: GarmentDao,
    private val historyDao: HistoryDao
) {
    val allGarments: Flow<List<GarmentItem>> = garmentDao.getAllGarmentItems()
    val allHistory: Flow<List<HistoryLog>> = historyDao.getAllHistory()

    fun searchGarments(query: String): Flow<List<GarmentItem>> {
        return if (query.isBlank()) {
            garmentDao.getAllGarmentItems()
        } else {
            garmentDao.searchGarments(query.trim())
        }
    }

    suspend fun getGarmentById(id: Long): GarmentItem? = garmentDao.getGarmentItemById(id)

    suspend fun insert(garment: GarmentItem): Long {
        val id = garmentDao.insertGarment(garment)
        logHistory(
            desc = "Added new garment: ${garment.categoryName} ${garment.subStyle}",
            garmentId = id,
            totalStockValue = garment.totalStock,
            actionType = "CREATE"
        )
        garment.stockPerSize.forEach { (size, stock) ->
            logHistory(
                desc = "Initial stock for size $size of ${garment.categoryName}: $stock",
                garmentId = id,
                size = size,
                stockValue = stock,
                totalStockValue = garment.totalStock,
                actionType = "CREATE"
            )
        }
        return id
    }

    suspend fun update(garment: GarmentItem) {
        logHistory(
            desc = "Updated garment details: ${garment.categoryName}",
            garmentId = garment.id,
            totalStockValue = garment.totalStock,
            actionType = "UPDATE_DETAILS"
        )
        garmentDao.updateGarment(garment)
    }

    // Feature 2: Persist undo delete stack via Room soft delete instead of in-memory list
    suspend fun delete(garment: GarmentItem) {
        logHistory(
            desc = "Deleted garment: ${garment.categoryName} ${garment.subStyle}",
            garmentId = garment.id,
            actionType = "DELETE"
        )
        garmentDao.softDeleteGarment(garment.id, System.currentTimeMillis())
    }

    suspend fun deleteById(id: Long) {
        garmentDao.softDeleteGarment(id, System.currentTimeMillis())
    }

    suspend fun restoreGarment(garment: GarmentItem) {
        logHistory(
            desc = "Restored deleted garment: ${garment.categoryName} ${garment.subStyle}",
            garmentId = garment.id,
            totalStockValue = garment.totalStock,
            actionType = "RESTORE"
        )
        garmentDao.restoreGarment(garment.id)
    }

    suspend fun restoreRecentlyDeleted(): GarmentItem? {
        val recent = garmentDao.getMostRecentlyDeleted() ?: return null
        restoreGarment(recent)
        return recent
    }

    suspend fun purgeOldDeleted(days: Int = 30) {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        garmentDao.purgeOldDeleted(cutoff)
    }

    val deletedGarments: Flow<List<GarmentItem>> = garmentDao.getDeletedGarments()

    // Feature 1: Low-stock query helper
    suspend fun getLowStockItems(globalThreshold: Int = 5): List<GarmentItem> {
        val all = garmentDao.getAllGarmentsList()
        return all.filter { item ->
            val threshold = item.lowStockThreshold ?: globalThreshold
            item.totalStock <= threshold || item.stockPerSize.values.any { it <= threshold }
        }
    }

    // Feature 3: JSON Backup & Restore
    suspend fun exportToJson(): String {
        val garments = garmentDao.getAllGarmentsList()
        val history = historyDao.getAllHistory()
        // Collect current history snapshot
        val historyList = mutableListOf<HistoryLog>()
        // Fetch up to 500 recent history entries
        val snapshot = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val root = org.json.JSONObject()
            root.put("version", 1)
            root.put("exportedAt", System.currentTimeMillis())

            val garmentsArray = org.json.JSONArray()
            garments.forEach { item ->
                val obj = org.json.JSONObject()
                obj.put("id", item.id)
                obj.put("categoryName", item.categoryName)
                obj.put("subStyle", item.subStyle)
                obj.put("sizeVariations", org.json.JSONArray(item.sizeVariations))
                obj.put("sizeTypeCategory", item.sizeTypeCategory)
                obj.put("handwrittenSizes", org.json.JSONArray(item.handwrittenSizes))
                obj.put("isPantModel", item.isPantModel)
                obj.put("notes", item.notes)
                obj.put("itemCode", item.itemCode)
                val stockObj = org.json.JSONObject()
                item.stockPerSize.forEach { (k, v) -> stockObj.put(k, v) }
                obj.put("stockPerSize", stockObj)
                obj.put("totalStock", item.totalStock)
                obj.put("updatedAt", item.updatedAt)
                if (item.lowStockThreshold != null) {
                    obj.put("lowStockThreshold", item.lowStockThreshold)
                }
                garmentsArray.put(obj)
            }
            root.put("garments", garmentsArray)
            root.toString(2)
        }
        return snapshot
    }

    suspend fun importFromJson(jsonString: String): Result<Int> = kotlin.runCatching {
        val root = org.json.JSONObject(jsonString)
        val garmentsArray = root.getJSONArray("garments")
        val importedGarments = mutableListOf<GarmentItem>()

        for (i in 0 until garmentsArray.length()) {
            val obj = garmentsArray.getJSONObject(i)
            val sizesArray = obj.getJSONArray("sizeVariations")
            val sizes = mutableListOf<String>()
            for (s in 0 until sizesArray.length()) sizes.add(sizesArray.getString(s))

            val hwArray = if (obj.has("handwrittenSizes")) obj.getJSONArray("handwrittenSizes") else org.json.JSONArray()
            val hw = mutableListOf<String>()
            for (h in 0 until hwArray.length()) hw.add(hwArray.getString(h))

            val stockObj = if (obj.has("stockPerSize")) obj.getJSONObject("stockPerSize") else org.json.JSONObject()
            val stockMap = mutableMapOf<String, Int>()
            val keys = stockObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                stockMap[key] = stockObj.getInt(key)
            }

            val lowThreshold = if (obj.has("lowStockThreshold") && !obj.isNull("lowStockThreshold")) {
                obj.getInt("lowStockThreshold")
            } else null

            importedGarments.add(
                GarmentItem(
                    id = if (obj.has("id")) obj.getLong("id") else 0L,
                    categoryName = obj.optString("categoryName", ""),
                    subStyle = obj.optString("subStyle", ""),
                    sizeVariations = sizes,
                    sizeTypeCategory = obj.optString("sizeTypeCategory", "Standard Alpha (XS-XXL)"),
                    handwrittenSizes = hw,
                    isPantModel = obj.optBoolean("isPantModel", false),
                    notes = obj.optString("notes", ""),
                    itemCode = obj.optString("itemCode", ""),
                    stockPerSize = stockMap,
                    totalStock = obj.optInt("totalStock", stockMap.values.sum()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                    lowStockThreshold = lowThreshold
                )
            )
        }

        if (importedGarments.isNotEmpty()) {
            garmentDao.insertAll(importedGarments)
            logHistory(
                desc = "Restored backup with ${importedGarments.size} garments",
                actionType = "BACKUP_RESTORE"
            )
        }
        importedGarments.size
    }

    suspend fun updateSizeStock(garmentId: Long, size: String, newStock: Int) {
        val garment = garmentDao.getGarmentItemById(garmentId) ?: return
        val currentStockMap = garment.stockPerSize.toMutableMap()
        val oldStock = currentStockMap[size] ?: 0
        currentStockMap[size] = newStock.coerceAtLeast(0)
        val newTotal = currentStockMap.values.sum()
        val updated = garment.copy(
            stockPerSize = currentStockMap,
            totalStock = newTotal,
            updatedAt = System.currentTimeMillis()
        )
        
        val diff = newStock - oldStock
        if (diff != 0) {
            val action = if (diff > 0) "Added $diff" else "Removed ${-diff}"
            logHistory(
                desc = "$action stock for size $size of ${garment.categoryName}",
                garmentId = garmentId,
                size = size,
                stockValue = newStock,
                totalStockValue = newTotal,
                actionType = "STOCK_UPDATE"
            )
        }
        
        garmentDao.updateGarment(updated)
    }
    
    fun recentHistory(days: Int = 30): Flow<List<HistoryLog>> {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return historyDao.getHistorySince(cutoff)
    }

    fun historyForGarment(garmentId: Long, days: Int = 30): Flow<List<HistoryLog>> {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return historyDao.getHistoryForGarment(garmentId, cutoff)
    }

    suspend fun logHistory(
        desc: String,
        garmentId: Long? = null,
        size: String? = null,
        stockValue: Int? = null,
        totalStockValue: Int? = null,
        actionType: String = "UPDATE"
    ) {
        historyDao.insertLog(
            HistoryLog(
                description = desc,
                garmentId = garmentId,
                size = size,
                stockValue = stockValue,
                totalStockValue = totalStockValue,
                actionType = actionType
            )
        )
    }
}
