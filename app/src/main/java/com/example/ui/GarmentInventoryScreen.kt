package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import com.example.ui.theme.LocalIsDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.GarmentItem
import com.example.ui.components.AddEditGarmentDialog
import com.example.ui.components.ExportDatabaseDialog
import com.example.ui.components.GarmentCard
import com.example.ui.components.NotificationSettingsDialog
import com.example.ui.components.StockDetailSheet
import com.example.ui.components.StockHistoryChartSheet
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleDarkSlate
import com.example.ui.theme.getCategorySwatchColor
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarmentInventoryScreen(
    viewModel: GarmentInventoryViewModel,
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var garmentToDelete by remember { mutableStateOf<GarmentItem?>(null) }

    // Feature 3: ActivityResult launchers for JSON backup export & import
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackupJson { jsonString ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(jsonString.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, "Database backup saved successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                } ?: ""
                viewModel.restoreBackupJson(
                    jsonString = jsonString,
                    onComplete = { count ->
                        Toast.makeText(context, "Restored $count garments successfully", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, "Restore error: $err", Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read backup: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val isDark = LocalIsDarkTheme.current
    // Static subtle textile gradient background - zero per-frame cost
    val textileGradient = remember(isDark) {
        Brush.verticalGradient(
            colors = if (isDark) listOf(Color(0xFF181716), Color(0xFF131211))
            else listOf(Color(0xFFFAF7F2), Color(0xFFF2ECE4))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(textileGradient)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            floatingActionButton = {
                // Bug 3: Wire up AnimatedVisibility with 200ms fade/scale transitions for smooth FAB appearance
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.9f, animationSpec = tween(200)),
                    exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.9f, animationSpec = tween(150))
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openAddDialog() },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = AppleDarkSlate,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                        modifier = Modifier.testTag("add_garment_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Garment",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Category",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Editorial Large Title Header Section
                item {
                    InventoryHeaderSection(
                        canUndoDelete = uiState.canUndoDelete,
                        onUndoDelete = { viewModel.undoDeleteRecentlyDeleted() },
                        onOpenSettings = { viewModel.openSettingsDialog() }
                    )
                }

                // Apple Minimalist Metric Overview (2x2 Grid)
                item {
                    AppleMetricGrid(
                        totalCategories = uiState.totalGarmentTypes,
                        totalVariations = uiState.totalVariationsCount,
                        pantModelsCount = uiState.isolatedPantModelsCount,
                        totalStock = uiState.totalInventoryStock
                    )
                }

                // Search Bar with narrowed state
                item {
                    InventorySearchBar(
                        query = uiState.searchQuery,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) }
                    )
                }

                // Filter Chips with Fabric Swatch Indicators
                item {
                    InventoryFilterChips(
                        activeFilter = uiState.activeFilter,
                        onFilterSelected = { viewModel.onFilterSelected(it) }
                    )
                }

                // Category Count Subtitle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GARMENT CATEGORIES (${uiState.filteredItems.size})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Empty State Check
                if (uiState.filteredItems.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "No Garments Found",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "No items match '${uiState.searchQuery}'. Try resetting your search.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // List of Garments with fast, cheap animateItem animations
                    items(
                        items = uiState.filteredItems,
                        key = { it.id }
                    ) { garment ->
                        GarmentCard(
                            item = garment,
                            globalLowStockThreshold = uiState.globalLowStockThreshold,
                            onStockClick = { viewModel.selectItemForStock(garment) },
                            onEditClick = { viewModel.openEditDialog(garment) },
                            onDeleteClick = { garmentToDelete = garment },
                            onGraphClick = { viewModel.openHistoryChart(garment) },
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(200),
                                fadeOutSpec = tween(150),
                                placementSpec = tween(200)
                            )
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }

        // Overlay Surfaces: Faux-glass styling and cheap 150-250ms transitions
        garmentToDelete?.let { garment ->
            AlertDialog(
                onDismissRequest = { garmentToDelete = null },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                title = {
                    Text(
                        text = "Delete Category",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete '${garment.categoryName}'? This action can be undone from the header.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteGarment(garment)
                            garmentToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { garmentToDelete = null }
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }

        // Stock Detail Bottom Sheet
        uiState.selectedItemForStock?.let { stockItem ->
            StockDetailSheet(
                item = stockItem,
                onDismiss = { viewModel.selectItemForStock(null) },
                onStockChange = { size, delta ->
                    viewModel.updateStock(stockItem.id, size, delta)
                }
            )
        }

        // Add / Edit Dialog with short 200ms fade/scale transition
        AnimatedVisibility(
            visible = uiState.isAddEditOpen,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200)),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.95f, animationSpec = tween(150))
        ) {
            AddEditGarmentDialog(
                itemToEdit = uiState.itemToEdit,
                onDismiss = { viewModel.closeAddEditDialog() },
                onSave = { category, subStyle, sizes, sizeCat, handwritten, isPant, notes, itemCode, existingId, lowStockThreshold ->
                    viewModel.saveGarment(
                        categoryName = category,
                        subStyle = subStyle,
                        sizes = sizes,
                        sizeCategory = sizeCat,
                        handwrittenSizes = handwritten,
                        isPantModel = isPant,
                        notes = notes,
                        itemCode = itemCode,
                        existingId = existingId,
                        lowStockThreshold = lowStockThreshold
                    )
                }
            )
        }

        // Stock History Chart Bottom Sheet
        uiState.selectedItemForChart?.let { chartItem ->
            StockHistoryChartSheet(
                item = chartItem,
                historyLogs = uiState.historyLogs,
                onDismiss = { viewModel.openHistoryChart(null) }
            )
        }

        // Settings Dialog with short 200ms transition
        AnimatedVisibility(
            visible = uiState.isSettingsOpen,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200)),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.95f, animationSpec = tween(150))
        ) {
            NotificationSettingsDialog(
                items = uiState.items,
                historyLogs = uiState.historyLogs,
                todaysHistoryLogs = uiState.todaysHistoryLogs,
                onNavigateToHistory = onNavigateToHistory,
                onDismiss = { viewModel.closeSettingsDialog() },
                onExportJsonBackup = { exportLauncher.launch("garment_inventory_backup.json") },
                onImportJsonBackup = { importLauncher.launch(arrayOf("application/json", "*/*")) }
            )
        }

        // Export Database Dialog with short 200ms transition
        AnimatedVisibility(
            visible = uiState.isExportOpen,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200)),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.95f, animationSpec = tween(150))
        ) {
            ExportDatabaseDialog(
                items = uiState.items,
                exportText = viewModel.getExportText(uiState.items),
                onDismiss = { viewModel.closeExportDialog() }
            )
        }
    }
}

// Scoped Sub-Composables for Narrowed State Reads and Smart Skipping
@Composable
private fun InventoryHeaderSection(
    canUndoDelete: Boolean,
    onUndoDelete: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val currentDate by produceState(
                    initialValue = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                ) {
                    while (true) {
                        delay(60_000L)
                        value = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                    }
                }
                Text(
                    text = "Inventory",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppleBlue,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
                Text(
                    text = "Extracted Categories & Variations • Rates Excluded",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Minimalist Header Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (canUndoDelete) 0.5f else 0.2f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    IconButton(
                        onClick = onUndoDelete,
                        enabled = canUndoDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("undo_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo Delete",
                            tint = if (canUndoDelete) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InventorySearchBar(
    query: String,
    onQueryChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search_garments_input"),
        placeholder = {
            Text(
                text = "Search category, style, or size...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChanged("") },
                    modifier = Modifier.testTag("clear_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            focusedBorderColor = AppleBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    )
}

@Composable
private fun InventoryFilterChips(
    activeFilter: GarmentFilter,
    onFilterSelected: (GarmentFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GarmentFilter.values().forEach { filter ->
            val isSelected = activeFilter == filter
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) AppleDarkSlate else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) AppleDarkSlate else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onFilterSelected(filter) }
                    .testTag("filter_chip_${filter.name}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fabric swatch indicator dot per filter
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(getCategorySwatchColor(filter.name))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = filter.displayName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AppleMetricGrid(
    totalCategories: Int,
    totalVariations: Int,
    pantModelsCount: Int,
    totalStock: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricCard(
            label = "CATEGORIES",
            value = "$totalCategories",
            subLabel = "Styles",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            label = "VARIATIONS",
            value = "$totalVariations",
            subLabel = "Alpha & Num",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            label = "PANT MODELS",
            value = "$pantModelsCount",
            subLabel = "Sizes 34–40",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            label = "TOTAL UNITS",
            value = "$totalStock",
            subLabel = "In Database",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    subLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
