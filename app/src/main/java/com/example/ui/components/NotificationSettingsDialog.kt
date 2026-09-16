package com.example.ui.components


import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Backup
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlarmScheduler
import com.example.MainActivity
import com.example.data.GarmentItem
import com.example.data.HistoryLog
import com.example.ui.PdfExporter

fun Context.findActivity(): MainActivity? = when (this) {
    is MainActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsDialog(
    items: List<GarmentItem>,
    historyLogs: List<HistoryLog>,
    todaysHistoryLogs: List<HistoryLog>,
    onNavigateToHistory: () -> Unit,
    onDismiss: () -> Unit,
    onExportJsonBackup: () -> Unit = {},
    onImportJsonBackup: () -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var title by remember { mutableStateOf(sharedPrefs.getString("notif_title", "Update Your Inventory") ?: "") }
    var lowStockThresholdText by remember { mutableStateOf(sharedPrefs.getInt("low_stock_threshold", 5).toString()) }
    val timeState = rememberTimePickerState(
        initialHour = sharedPrefs.getInt("notif_hour", 20),
        initialMinute = sharedPrefs.getInt("notif_minute", 0),
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        modifier = Modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                val currentTheme by com.example.MainActivity.themeModeFlow.collectAsState()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("system", "light", "dark").forEach { mode ->
                        FilterChip(
                            selected = currentTheme == mode,
                            onClick = { 
                                 sharedPrefs.edit().putString("theme_mode", mode).apply()
                                com.example.MainActivity.themeModeFlow.value = mode
                            },
                            label = { Text(mode.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(text = "Notification Reminder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notification Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    TimeInput(state = timeState)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Feature 1: Global Low-Stock Alert Threshold
                Text(
                    text = "Stock Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = lowStockThresholdText,
                    onValueChange = { lowStockThresholdText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Global Low-Stock Alert Threshold (Units)") },
                    placeholder = { Text("5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Activity Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = { 
                        onDismiss()
                        onNavigateToHistory()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View History")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Data Export & Backup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = { PdfExporter.exportToPdf(context, items) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export Full Inventory",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Full Inventory (PDF)")
                }
                
                Button(
                    onClick = { PdfExporter.exportHistoryToPdf(context, todaysHistoryLogs, "Today", items) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export Today's History",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Today's History (PDF)")
                }

                // Feature 3: JSON Full Database Backup & Restore
                Button(
                    onClick = {
                        onDismiss()
                        onExportJsonBackup()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = "Backup Database (JSON)",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup Database (JSON)")
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onImportJsonBackup()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Restore Database (JSON)",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore Database (JSON)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val oldHour = sharedPrefs.getInt("notif_hour", 20)
                    val oldMinute = sharedPrefs.getInt("notif_minute", 0)
                    val timeChanged = oldHour != timeState.hour || oldMinute != timeState.minute
                    val threshold = lowStockThresholdText.toIntOrNull() ?: 5

                    sharedPrefs.edit()
                        .putString("notif_title", title)
                        .putInt("notif_hour", timeState.hour)
                        .putInt("notif_minute", timeState.minute)
                        .putInt("low_stock_threshold", threshold)
                        .apply()
                    
                    // Bug 1: Only reschedule when the saved hour/minute actually changed
                    if (timeChanged) {
                        AlarmScheduler.scheduleDailyAlarm(context, timeState.hour, timeState.minute)
                    }

                    // Bug 1: On API 31+, handle revoked exact-alarm capability and guide to system settings
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !AlarmScheduler.canScheduleExact(context)) {
                        Toast.makeText(
                            context,
                            "Please allow 'Alarms & reminders' for precise daily stock alerts.",
                            Toast.LENGTH_LONG
                        ).show()
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }

                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
