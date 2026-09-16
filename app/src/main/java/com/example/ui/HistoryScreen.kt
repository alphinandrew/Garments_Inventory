package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HistoryLog
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.LocalIsDarkTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: GarmentInventoryViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val garments = uiState.items
    val allLogs = uiState.historyLogs
    val context = LocalContext.current

    
    val dayPages = remember(allLogs, garments) {
        // Bug 2: Compute consolidated day pages including today and gap days,
        // displaying carried-forward values when no logs were recorded.
        com.example.data.HistoryGrouping.computeConsolidatedDayPages(garments, allLogs)
    }

    val pagerState = rememberPagerState(pageCount = { dayPages.size })

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("history_screen_root"),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Stock History",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (dayPages.isNotEmpty()) {
                        IconButton(onClick = {
                            val currentPage = dayPages[pagerState.currentPage]
                            PdfExporter.exportDayHistoryToPdf(context, currentPage)
                        }, modifier = Modifier.testTag("export_pdf_button")) {
                            Icon(Icons.Default.Download, contentDescription = "Export PDF")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ),
                modifier = Modifier
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val isDark = LocalIsDarkTheme.current
        val textileGradient = remember(isDark) {
            androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = if (isDark) listOf(Color(0xFF181716), Color(0xFF131211))
                else listOf(Color(0xFFFAF7F2), Color(0xFFF2ECE4))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(textileGradient)
        ) {
            if (dayPages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No history available.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f).testTag("history_pager")
                    ) { pageIndex ->
                        val page = dayPages[pageIndex]
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Column {
                                    Text(
                                        text = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(page.dateMs)),
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AppleBlue,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    // Bug 2: Clearly indicate when values are carried forward from previous entry
                                    if (page.isCarriedForward) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            ),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(MaterialTheme.colorScheme.onSurfaceVariant, androidx.compose.foundation.shape.CircleShape)
                                                )
                                                Text(
                                                    text = "No changes — carried from previous entry",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            items(page.entries) { entry ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = entry.garmentName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        entry.sizeToStock.forEach { (size, stock) ->
                                            Text(
                                                text = "$size: $stock units",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Total: ${entry.sizeToStock.values.sum()} units",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Page indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Day ${pagerState.currentPage + 1} of ${dayPages.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
