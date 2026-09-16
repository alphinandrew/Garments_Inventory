package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GarmentItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.TagHandwrittenBg
import com.example.ui.theme.TagHandwrittenBorder
import com.example.ui.theme.TagHandwrittenText
import com.example.ui.theme.TagPantBg
import com.example.ui.theme.TagPantBorder
import com.example.ui.theme.TagPantText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailSheet(
    item: GarmentItem,
    onDismiss: () -> Unit,
    onStockChange: (size: String, newValue: Int) -> Unit
    
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Title, Sub-Style & Dismiss Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.categoryName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.subStyle.isNotBlank()) {
                        Text(
                            text = item.subStyle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(32.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_stock_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Apple Health / iOS Style Metric Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL UNITS IN STOCK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${item.totalStock}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SIZE VARIATIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${item.sizeVariations.size} sizes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = AppleBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "UNIT COUNTS BY SIZE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // iOS Grouped List Style for Size Breakdown
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = item.sizeVariations,
                    key = { it }
                ) { size ->
                    val currentStock = item.stockPerSize[size] ?: 0
                    val isHandwritten = item.handwrittenSizes.contains(size)
                    val isPantWaist = item.isPantModel && size in listOf("34", "36", "38", "40")

                    StockRowItem(
                        size = size,
                        currentStock = currentStock,
                        isHandwritten = isHandwritten,
                        isPantWaist = isPantWaist,
                        onStockUpdate = { newValue -> onStockChange(size, newValue) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("done_stock_sheet_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppleBlue
                )
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun StockRowItem(
    size: String,
    currentStock: Int,
    isHandwritten: Boolean,
    isPantWaist: Boolean,
    onStockUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Size identifier & badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isHandwritten -> TagHandwrittenBg
                        isPantWaist -> TagPantBg
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isHandwritten -> TagHandwrittenBorder
                            isPantWaist -> TagPantBorder
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        }
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = size,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = when {
                                isHandwritten -> TagHandwrittenText
                                isPantWaist -> TagPantText
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                Column {
                    Text(
                        text = "Size $size",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isHandwritten) {
                        Text(
                            text = "Handwritten addition",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TagHandwrittenText
                        )
                    } else if (isPantWaist) {
                        Text(
                            text = "Pant waist size",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TagPantText
                        )
                    }
                }
            }

            // Apple Stepper style controls (- Count +)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Manual Input Container
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    var isFocused by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                    var textValue by androidx.compose.runtime.remember(size) {
                        androidx.compose.runtime.mutableStateOf(
                            androidx.compose.ui.text.input.TextFieldValue(
                                text = if (currentStock == 0) "" else currentStock.toString()
                            )
                        )
                    }

                    // Bug 4: Originally, onValueChange called onStockUpdate() immediately on every digit typed.
                    // Typing "120" fired 3 separate DB writes and 3 history logs, while racing against this LaunchedEffect.
                    // Fix: While focused, local text state is the source of truth. We only sync from DB when not focused.
                    androidx.compose.runtime.LaunchedEffect(currentStock, isFocused) {
                        if (!isFocused) {
                            textValue = textValue.copy(text = if (currentStock == 0) "" else currentStock.toString())
                        }
                    }

                    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
                    val commitStockChange = {
                        val parsed = textValue.text.toIntOrNull() ?: 0
                        if (parsed != currentStock) {
                            onStockUpdate(parsed)
                        }
                    }

                    // Ensure uncommitted edits are flushed if sheet or row is disposed while focused
                    androidx.compose.runtime.DisposableEffect(size) {
                        onDispose {
                            if (isFocused) {
                                commitStockChange()
                            }
                        }
                    }

                    androidx.compose.foundation.text.BasicTextField(
                        value = textValue,
                        onValueChange = { newValue ->
                            // Preserve numeric-only filtering without writing to DB on every keystroke
                            val filteredText = newValue.text.filter { it.isDigit() }
                            textValue = newValue.copy(text = filteredText)
                        },
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = {
                                commitStockChange()
                                focusManager.clearFocus()
                            }
                        ),
                        modifier = Modifier
                            .width(60.dp)
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .onFocusChanged { focusState ->
                                if (isFocused && !focusState.isFocused) {
                                    // Focus lost: commit final committed value to database
                                    commitStockChange()
                                }
                                isFocused = focusState.isFocused
                            }
                            .testTag("stock_input_$size")
                    )
                }
            }
        }
    }
}
