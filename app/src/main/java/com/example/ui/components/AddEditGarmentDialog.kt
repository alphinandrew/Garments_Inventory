package com.example.ui.components
import androidx.compose.foundation.border

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GarmentItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.TagHandwrittenBg
import com.example.ui.theme.TagHandwrittenBorder
import com.example.ui.theme.TagHandwrittenText
import com.example.ui.theme.TagSizeRegularBg
import com.example.ui.theme.TagSizeRegularBorder
import com.example.ui.theme.TagSizeRegularText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditGarmentDialog(
    itemToEdit: GarmentItem?,
    onDismiss: () -> Unit,
    onSave: (
        categoryName: String,
        subStyle: String,
        sizes: List<String>,
        sizeCategory: String,
        handwrittenSizes: List<String>,
        isPantModel: Boolean,
        notes: String,
        itemCode: String,
        existingId: Long,
        lowStockThreshold: Int?
    ) -> Unit
) {
    var categoryName by remember { mutableStateOf(itemToEdit?.categoryName ?: "") }
    var subStyle by remember { mutableStateOf(itemToEdit?.subStyle ?: "") }
    var itemCode by remember { mutableStateOf(itemToEdit?.itemCode ?: "") }
    var notes by remember { mutableStateOf(itemToEdit?.notes ?: "") }
    var isPantModel by remember { mutableStateOf(itemToEdit?.isPantModel ?: false) }
    var lowStockThresholdInput by remember {
        mutableStateOf(itemToEdit?.lowStockThreshold?.toString() ?: "")
    }

    var selectedSizes by remember {
        mutableStateOf(itemToEdit?.sizeVariations ?: listOf("S", "M", "L", "XL"))
    }
    var handwrittenSizes by remember {
        mutableStateOf(itemToEdit?.handwrittenSizes ?: emptyList())
    }

    var customSizeInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetSizeGroups = listOf(
        "Girls Tshirts" to listOf("12", "XS", "S", "M", "L", "XL", "XXL"),
        "Ladies Tshirts" to listOf("S", "M", "L", "XL", "XXL", "3XL"),
        "Girls Night Set" to listOf("12", "XS", "S", "M", "L", "XL", "XXL"),
        "Girls Frock" to listOf("XS", "S", "M", "L"),
        "Ladies Night Set" to listOf("S", "M", "L", "XL", "XXL", "3XL", "4XL", "5XL"),
        "Feeding Frock" to listOf("M", "L", "XL", "XXL"),
        "Ladies Pants" to listOf("34", "36", "38", "40")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        title = {
            Text(
                text = if (itemToEdit == null) "New Category" else "Edit Item",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Category Name
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        errorMessage = null
                    },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Ladies Night Set, Girls Tshirts") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_category_name"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue
                    )
                )

                // Sub-Style
                OutlinedTextField(
                    value = subStyle,
                    onValueChange = { subStyle = it },
                    label = { Text("Sub-Style (in brackets)") },
                    placeholder = { Text("Plain Set, AOP Set, Collar Set, Ankle Length") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sub_style"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue
                    )
                )

                // Item Code
                OutlinedTextField(
                    value = itemCode,
                    onValueChange = { itemCode = it },
                    label = { Text("Item Code / SKU (Optional)") },
                    placeholder = { Text("e.g. LNS-PLN, GTS-001") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_item_code"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue
                    )
                )

                // Pant Model Switch Row
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pant / Bottom Model",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Isolates waist sizes (34, 36, 38, 40) exclusively",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isPantModel,
                            onCheckedChange = {
                                isPantModel = it
                                if (it && !selectedSizes.contains("34")) {
                                    selectedSizes = listOf("34", "36", "38", "40")
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppleBlue
                            ),
                            modifier = Modifier.testTag("switch_is_pant")
                        )
                    }
                }

                // Quick Size Presets
                Text(
                    text = "SIZE PRESETS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetSizeGroups.forEach { (label, sizes) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clickable {
                                    val combined = (selectedSizes + sizes).distinct()
                                    selectedSizes = combined
                                }
                        ) {
                            Text(
                                text = "+ $label",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Custom Size Add Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customSizeInput,
                        onValueChange = { customSizeInput = it },
                        placeholder = { Text("Add custom size (e.g. 3XL, 10)...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_custom_size"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleBlue
                        )
                    )
                    Button(
                        onClick = {
                            if (customSizeInput.isNotBlank()) {
                                val trimmed = customSizeInput.trim()
                                if (!selectedSizes.contains(trimmed)) {
                                    selectedSizes = selectedSizes + trimmed
                                }
                                customSizeInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("add_custom_size_button")
                    ) {
                        Text("Add")
                    }
                }

                // Selected Sizes Matrix & Handwritten Marker
                Text(
                    text = "SELECTED SIZES (${selectedSizes.size}) • Tap to toggle ✍️ handwritten",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    selectedSizes.forEach { size ->
                        val isHandwritten = handwrittenSizes.contains(size)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isHandwritten) TagHandwrittenBg else TagSizeRegularBg,
                            border = BorderStroke(
                                1.dp,
                                if (isHandwritten) TagHandwrittenBorder else TagSizeRegularBorder
                            ),
                            modifier = Modifier.clickable {
                                handwrittenSizes = if (isHandwritten) {
                                    handwrittenSizes - size
                                } else {
                                    handwrittenSizes + size
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = size,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isHandwritten) TagHandwrittenText else TagSizeRegularText
                                )
                                if (isHandwritten) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .padding(end = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "•", color = TagHandwrittenText, fontWeight = FontWeight.Bold)
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        selectedSizes = selectedSizes - size
                                        handwrittenSizes = handwrittenSizes - size
                                    },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(11.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Feature 1: Optional low-stock alert threshold per garment
                OutlinedTextField(
                    value = lowStockThresholdInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            lowStockThresholdInput = input
                        }
                    },
                    label = { Text("Low-Stock Alert Threshold (Optional)") },
                    placeholder = { Text("e.g. 5 (Leave blank to use global)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_low_stock_threshold"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue
                    )
                )

                // Notes field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Specifications") },
                    placeholder = { Text("Prices excluded, handwritten notes...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isBlank()) {
                        errorMessage = "Please enter a garment category name."
                        return@Button
                    }
                    if (selectedSizes.isEmpty()) {
                        errorMessage = "Please select at least one size variation."
                        return@Button
                    }
                    val sizeCat = if (isPantModel) "Pant Waist (34-40)"
                    else if (selectedSizes.any { it in listOf("12", "14", "16", "18", "20") }) "Girls/Kids Numeric"
                    else "Standard Alpha"

                    val threshold = lowStockThresholdInput.toIntOrNull()

                    onSave(
                        categoryName,
                        subStyle,
                        selectedSizes,
                        sizeCat,
                        handwrittenSizes,
                        isPantModel,
                        notes,
                        itemCode,
                        itemToEdit?.id ?: 0L,
                        threshold
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                modifier = Modifier.testTag("save_garment_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_garment_button")
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
