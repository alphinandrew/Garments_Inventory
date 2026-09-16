package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GarmentDatabase
import com.example.data.GarmentItem
import com.example.data.GarmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class GarmentFilter(val displayName: String) {
    ALL("All Garments"),
    GIRLS("Girls Wear"),
    NIGHT_SETS("Night Sets"),
    FROCKS("Feeding / Frocks"),
    TOPS("Tops & Kurtis"),
    PANTS_BOTTOMS("Pants & Bottoms (34-40)"),
    NIGHTIES("Nighties")
}

// Bug 3: Mark InventoryUiState as @Immutable to enable Compose compiler smart skipping
@androidx.compose.runtime.Immutable
data class InventoryUiState(
    val items: List<GarmentItem> = emptyList(),
    val historyLogs: List<com.example.data.HistoryLog> = emptyList(),
    val todaysHistoryLogs: List<com.example.data.HistoryLog> = emptyList(),
    val filteredItems: List<GarmentItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: GarmentFilter = GarmentFilter.ALL,
    val selectedItemForStock: GarmentItem? = null,
    val selectedItemForChart: GarmentItem? = null,
    val isAddEditOpen: Boolean = false,
    val itemToEdit: GarmentItem? = null,
    val isExportOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val totalGarmentTypes: Int = 0,
    val totalVariationsCount: Int = 0,
    val totalInventoryStock: Int = 0,
    val isolatedPantModelsCount: Int = 0,
    val canUndoDelete: Boolean = false,
    val globalLowStockThreshold: Int = 5
)

class GarmentInventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GarmentRepository
    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(GarmentFilter.ALL)

    init {
        val db = GarmentDatabase.getDatabase(application, viewModelScope)
        repository = GarmentRepository(db.garmentDao(), db.historyDao())

        // Ensure database has initial data if empty and purge old deleted items
        viewModelScope.launch {
            repository.purgeOldDeleted(30)
            if (db.garmentDao().getItemCount() == 0) {
                db.garmentDao().insertAll(com.example.data.InitialGarmentData.getInitialGarments())
            } else {
                repository.allGarments.first().forEach { item ->
                    if (item.id in listOf(2L, 3L, 4L, 9L)) {
                        val missingSizes = listOf("4XL", "5XL").filter { !item.sizeVariations.contains(it) }
                        if (missingSizes.isNotEmpty()) {
                            val newSizes = item.sizeVariations + missingSizes
                            val newHandwritten = (item.handwrittenSizes + missingSizes).distinct()
                            val newStockMap = item.stockPerSize.toMutableMap()
                            missingSizes.forEach { newStockMap[it] = 10 }
                            
                            repository.update(item.copy(
                                sizeVariations = newSizes,
                                handwrittenSizes = newHandwritten,
                                stockPerSize = newStockMap,
                                totalStock = newStockMap.values.sum()
                            ))
                        }
                    }
                }
            }
        }
    }

    private data class DialogState(
        val selectedItemForStock: GarmentItem? = null,
        val isAddEditOpen: Boolean = false,
        val itemToEdit: GarmentItem? = null,
        val isExportOpen: Boolean = false,
        val isSettingsOpen: Boolean = false,
        val selectedItemForChart: GarmentItem? = null
    )

    private val _dialogState = MutableStateFlow(DialogState())

    private val _itemsAndFilterFlow = combine(
        repository.allGarments,
        _searchQuery,
        _activeFilter
    ) { allItems, query, filter ->
        val filtered = allItems.filter { item ->
            val matchesFilter = when (filter) {
                GarmentFilter.ALL -> true
                GarmentFilter.GIRLS -> item.categoryName.contains("Girls", ignoreCase = true)
                GarmentFilter.NIGHT_SETS -> item.categoryName.contains("Night Set", ignoreCase = true)
                GarmentFilter.FROCKS -> item.categoryName.contains("Frock", ignoreCase = true) || item.categoryName.contains("Feeding", ignoreCase = true)
                GarmentFilter.TOPS -> item.categoryName.contains("Top", ignoreCase = true) || item.categoryName.contains("Kurti", ignoreCase = true)
                GarmentFilter.PANTS_BOTTOMS -> item.isPantModel || item.categoryName.contains("Pant", ignoreCase = true) || item.categoryName.contains("Palazzo", ignoreCase = true)
                GarmentFilter.NIGHTIES -> item.categoryName.contains("Nightie", ignoreCase = true)
            }

            val matchesQuery = if (query.isBlank()) true else {
                item.categoryName.contains(query, ignoreCase = true) ||
                        item.subStyle.contains(query, ignoreCase = true) ||
                        item.sizeVariations.any { it.contains(query, ignoreCase = true) } ||
                        item.notes.contains(query, ignoreCase = true) ||
                        item.itemCode.contains(query, ignoreCase = true)
            }

            matchesFilter && matchesQuery
        }

        Triple(allItems, filtered, Pair(query, filter))
    }

    val uiState: StateFlow<InventoryUiState> = combine(
        _itemsAndFilterFlow,
        _dialogState,
        repository.recentHistory(30),
        repository.deletedGarments
    ) { (allItems, filtered, queryAndFilter), dialogState, history, deletedItems ->
        val (query, filter) = queryAndFilter
        val updatedSelectedItem = if (dialogState.selectedItemForStock != null) {
            allItems.find { it.id == dialogState.selectedItemForStock.id } ?: dialogState.selectedItemForStock
        } else null

        val updatedChartItem = if (dialogState.selectedItemForChart != null) {
            allItems.find { it.id == dialogState.selectedItemForChart.id } ?: dialogState.selectedItemForChart
        } else null

        val startOfToday = com.example.data.HistoryGrouping.getMidnightMs(System.currentTimeMillis())
        val todaysHistory = history.filter { it.timestamp >= startOfToday }

        val totalVariations = allItems.sumOf { it.sizeVariations.size }
        val totalStock = allItems.sumOf { it.totalStock }
        val pantCount = allItems.count { it.isPantModel }

        val sharedPrefs = getApplication<Application>().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
        val globalThreshold = sharedPrefs.getInt("low_stock_threshold", 5)

        InventoryUiState(
            items = allItems,
            historyLogs = history,
            todaysHistoryLogs = todaysHistory,
            filteredItems = filtered,
            searchQuery = query,
            activeFilter = filter,
            selectedItemForStock = updatedSelectedItem,
            selectedItemForChart = updatedChartItem,
            isAddEditOpen = dialogState.isAddEditOpen,
            itemToEdit = dialogState.itemToEdit,
            isExportOpen = dialogState.isExportOpen,
            isSettingsOpen = dialogState.isSettingsOpen,
            totalGarmentTypes = allItems.size,
            totalVariationsCount = totalVariations,
            totalInventoryStock = totalStock,
            isolatedPantModelsCount = pantCount,
            canUndoDelete = deletedItems.isNotEmpty(),
            globalLowStockThreshold = globalThreshold
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InventoryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: GarmentFilter) {
        _activeFilter.value = filter
    }

    fun selectItemForStock(item: GarmentItem?) {
        _dialogState.value = _dialogState.value.copy(selectedItemForStock = item)
    }

    fun openHistoryChart(item: GarmentItem?) {
        _dialogState.value = _dialogState.value.copy(selectedItemForChart = item)
    }

    fun openAddDialog() {
        _dialogState.value = _dialogState.value.copy(
            itemToEdit = null,
            isAddEditOpen = true
        )
    }

    fun openEditDialog(item: GarmentItem) {
        _dialogState.value = _dialogState.value.copy(
            itemToEdit = item,
            isAddEditOpen = true
        )
    }

    fun closeAddEditDialog() {
        _dialogState.value = _dialogState.value.copy(
            isAddEditOpen = false,
            itemToEdit = null
        )
    }

    fun openExportDialog() {
        _dialogState.value = _dialogState.value.copy(isExportOpen = true)
    }

    fun closeExportDialog() {
        _dialogState.value = _dialogState.value.copy(isExportOpen = false)
    }

    fun openSettingsDialog() {
        _dialogState.value = _dialogState.value.copy(isSettingsOpen = true)
    }

    fun closeSettingsDialog() {
        _dialogState.value = _dialogState.value.copy(isSettingsOpen = false)
    }

    fun saveGarment(
        categoryName: String,
        subStyle: String,
        sizes: List<String>,
        sizeCategory: String,
        handwrittenSizes: List<String>,
        isPantModel: Boolean,
        notes: String,
        itemCode: String,
        existingId: Long = 0,
        lowStockThreshold: Int? = null
    ) {
        viewModelScope.launch {
            val initialStockMap = mutableMapOf<String, Int>()
            sizes.forEach { size ->
                initialStockMap[size] = 20
            }
            val total = initialStockMap.values.sum()

            if (existingId == 0L) {
                val newItem = GarmentItem(
                    categoryName = categoryName.trim(),
                    subStyle = subStyle.trim(),
                    sizeVariations = sizes,
                    sizeTypeCategory = sizeCategory,
                    handwrittenSizes = handwrittenSizes,
                    isPantModel = isPantModel,
                    notes = notes.trim(),
                    itemCode = itemCode.ifBlank { "G-${System.currentTimeMillis().toString().takeLast(4)}" },
                    stockPerSize = initialStockMap,
                    totalStock = total,
                    updatedAt = System.currentTimeMillis(),
                    lowStockThreshold = lowStockThreshold
                )
                repository.insert(newItem)
            } else {
                val current = repository.getGarmentById(existingId)
                val updatedStockMap = current?.stockPerSize?.toMutableMap() ?: initialStockMap
                // ensure all new sizes exist in stock map
                sizes.forEach { size ->
                    if (!updatedStockMap.containsKey(size)) {
                        updatedStockMap[size] = 0
                    }
                }
                val updatedItem = GarmentItem(
                    id = existingId,
                    categoryName = categoryName.trim(),
                    subStyle = subStyle.trim(),
                    sizeVariations = sizes,
                    sizeTypeCategory = sizeCategory,
                    handwrittenSizes = handwrittenSizes,
                    isPantModel = isPantModel,
                    notes = notes.trim(),
                    itemCode = itemCode.ifBlank { current?.itemCode ?: "G-000" },
                    stockPerSize = updatedStockMap,
                    totalStock = updatedStockMap.values.sum(),
                    updatedAt = System.currentTimeMillis(),
                    lowStockThreshold = lowStockThreshold ?: current?.lowStockThreshold
                )
                repository.update(updatedItem)
            }
            closeAddEditDialog()
        }
    }

    // Feature 2: Persisted undo delete - deletes via Room soft-delete so it survives process death
    fun deleteGarment(garment: GarmentItem) {
        viewModelScope.launch {
            repository.delete(garment)
            if (_dialogState.value.selectedItemForStock?.id == garment.id) {
                _dialogState.value = _dialogState.value.copy(selectedItemForStock = null)
            }
        }
    }

    // Feature 2: Persisted undo delete - restores last deleted item from Room
    fun undoDeleteRecentlyDeleted() {
        viewModelScope.launch {
            repository.restoreRecentlyDeleted()
        }
    }

    // Feature 3: JSON Backup and Restore
    fun exportBackupJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportToJson()
            onReady(json)
        }
    }

    fun restoreBackupJson(jsonString: String, onComplete: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.importFromJson(jsonString)
            result.onSuccess { count -> onComplete(count) }
                .onFailure { err -> onError(err.message ?: "Failed to parse backup") }
        }
    }

    fun updateStock(garmentId: Long, size: String, newStock: Int) {
        viewModelScope.launch {
            repository.updateSizeStock(garmentId, size, newStock.coerceAtLeast(0))
        }
    }

    fun getExportText(items: List<GarmentItem>): String {
        val sb = StringBuilder()
        sb.append("=== GARMENT INVENTORY DATABASE (CATEGORIES & SIZES) ===\n")
        sb.append("Generated at: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())}\n")
        sb.append("Prices: STRICTLY EXCLUDED as instructed\n\n")

        items.forEachIndexed { index, item ->
            sb.append("${index + 1}. Item Category: ${item.categoryName}")
            if (item.subStyle.isNotBlank()) {
                sb.append(" (${item.subStyle})")
            }
            sb.append("\n")
            sb.append("   - Size Variations: ${item.sizeVariations.joinToString(", ")}\n")
            if (item.handwrittenSizes.isNotEmpty()) {
                sb.append("   - Handwritten Additions: ${item.handwrittenSizes.joinToString(", ")}\n")
            }
            if (item.isPantModel) {
                sb.append("   - Type: Isolated Pant Model (Waist sizes: 34, 36, 38, 40)\n")
            }
            if (item.notes.isNotBlank()) {
                sb.append("   - Notes: ${item.notes}\n")
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}
