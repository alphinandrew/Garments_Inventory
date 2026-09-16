package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GarmentItem
import com.example.data.HistoryLog
import com.example.data.HistoryGrouping
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockHistoryChartSheet(
    item: GarmentItem,
    historyLogs: List<HistoryLog>,
    
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedSize by remember { mutableStateOf<String?>("Total") }
    val sizes = remember(item) { listOf("Total") + item.sizeVariations }

    val currentStock = remember(item, selectedSize) {
        if (selectedSize == "Total") item.totalStock else (item.stockPerSize[selectedSize] ?: 0)
    }

    val dailyData = remember(historyLogs, selectedSize, currentStock) {
        HistoryGrouping.resolveFinalValuePerDay(
            logs = historyLogs,
            isTotal = selectedSize == "Total",
            size = selectedSize,
            fallbackStock = currentStock
        )
    }

    var selectedPointIndex by remember(dailyData) { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${item.categoryName}${if(item.subStyle.isNotBlank()) " - ${item.subStyle}" else ""} Stock History",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (sizes.size > 2) {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sizes.size) { index ->
                        val size = sizes[index]
                        FilterChip(
                            selected = size == selectedSize,
                            onClick = { selectedSize = size },
                            label = { Text(size) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            if (dailyData.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No history available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val lineColor = MaterialTheme.colorScheme.primary
                val textMeasurer = rememberTextMeasurer()
                val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                val density = LocalDensity.current
                val maxStock = remember(dailyData) { dailyData.maxOfOrNull { it.second }?.coerceAtLeast(10) ?: 10 }
                val bottomPadding = with(density) { 30.dp.toPx() }
                val topPadding = with(density) { 20.dp.toPx() }
                val rightPadding = with(density) {
                    val textWidth = textMeasurer.measure(maxStock.toString(), labelStyle).size.width.toFloat()
                    textWidth + 12.dp.toPx()
                }
                
                val points = remember(dailyData, canvasSize, maxStock, bottomPadding, topPadding, rightPadding) {
                    if (canvasSize.width == 0 || canvasSize.height == 0) return@remember emptyList<Offset>()
                    
                    val width = canvasSize.width.toFloat()
                    val height = canvasSize.height.toFloat()
                    
                    val drawableWidth = width - rightPadding
                    val drawableHeight = height - bottomPadding - topPadding

                    val dayCount = dailyData.size
                    
                    dailyData.mapIndexed { index, data ->
                        val stock = data.second
                        val x = if (dayCount > 1) {
                            (index.toFloat() / (dayCount - 1)) * drawableWidth
                        } else {
                            drawableWidth / 2f
                        }
                        val y = topPadding + (drawableHeight - ((stock.toFloat() / maxStock) * drawableHeight))
                        Offset(x, y)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("graph_canvas")
                            .onSizeChanged { canvasSize = it }
                            .pointerInput(points) {
                                detectTapGestures { tapOffset ->
                                    if (points.isEmpty()) return@detectTapGestures
                                    val nearestIndex = points.withIndex().minByOrNull { abs(it.value.x - tapOffset.x) }?.index
                                    
                                    if (nearestIndex != null && selectedPointIndex == nearestIndex) {
                                        selectedPointIndex = null
                                    } else {
                                        selectedPointIndex = nearestIndex
                                    }
                                }
                            }
                    ) {
                        if (points.isEmpty()) return@Canvas
                        val drawableWidth = size.width - rightPadding
                        val drawableHeight = size.height - bottomPadding - topPadding

                        // Draw Grid Lines & Y-Axis Labels
                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val y = topPadding + drawableHeight - (i.toFloat() / gridLines * drawableHeight)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.3f),
                                start = Offset(0f, y),
                                end = Offset(drawableWidth, y),
                                strokeWidth = 1f
                            )
                            val labelText = (i * maxStock / gridLines).toString()
                            val textLayoutResult = textMeasurer.measure(labelText, labelStyle)
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = Offset(drawableWidth + 4.dp.toPx(), y - (textLayoutResult.size.height / 2f))
                            )
                        }

                        val path = Path()
                        path.moveTo(points.first().x, points.first().y)
                        
                        // Bezier smoothing
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2f
                            path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }
                        
                        // Draw Gradient Fill
                        val fillPath = Path()
                        fillPath.addPath(path)
                        fillPath.lineTo(points.last().x, size.height - bottomPadding)
                        fillPath.lineTo(points.first().x, size.height - bottomPadding)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                                startY = topPadding,
                                endY = size.height - bottomPadding
                            )
                        )

                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                        
                        points.forEachIndexed { index, point ->
                            drawCircle(
                                color = lineColor,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = point
                            )
                            
                            val dayText = SimpleDateFormat("d", Locale.getDefault()).format(Date(dailyData[index].first))
                            val textLayoutResult = textMeasurer.measure(dayText, labelStyle)
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = Offset(point.x - (textLayoutResult.size.width / 2f), size.height - textLayoutResult.size.height)
                            )
                        }
                    }
                    
                    selectedPointIndex?.let { index ->
                        if (index in points.indices) {
                            val point = points[index]
                            val data = dailyData[index]
                            val stock = data.second
                            val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(data.first))
                            
                            val tooltipHeight = 80f // Approx height in px
                            var yPos = point.y.toInt() - tooltipHeight.toInt() - 20
                            if (yPos < 0) {
                                yPos = point.y.toInt() + 20 // Flip below
                            }

                            Box(
                                modifier = Modifier
                                    .offset { 
                                        IntOffset(
                                            x = (point.x.toInt() - 60).coerceIn(0, (canvasSize.width - 120).coerceAtLeast(0)), 
                                            y = yPos 
                                        ) 
                                    }
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$dateStr • $stock units",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
