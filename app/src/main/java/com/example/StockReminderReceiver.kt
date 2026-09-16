package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.data.GarmentDatabase
import com.example.data.GarmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Bug 1: BroadcastReceiver triggered by AlarmManager to show notifications at the exact scheduled time
// and immediately re-schedule the next day's alarm.
// Feature 1: Checks for low-stock items and highlights them in the reminder message.
class StockReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                val hour = sharedPrefs.getInt("notif_hour", 20)
                val minute = sharedPrefs.getInt("notif_minute", 0)
                val title = sharedPrefs.getString("notif_title", "Update Your Inventory") ?: "Update Your Inventory"
                val globalThreshold = sharedPrefs.getInt("low_stock_threshold", 5)

                // Feature 1: Check low stock items from repository
                val db = GarmentDatabase.getDatabase(context, scope)
                val repo = GarmentRepository(db.garmentDao(), db.historyDao())
                val lowStockItems = repo.getLowStockItems(globalThreshold)

                val message = if (lowStockItems.isNotEmpty()) {
                    val names = lowStockItems.take(3).joinToString { it.categoryName }
                    if (lowStockItems.size > 3) {
                        "Low stock on $names and ${lowStockItems.size - 3} more. Tap to review."
                    } else {
                        "Low stock alert: $names below threshold. Tap to review."
                    }
                } else {
                    "Don't forget to update your garment stock levels."
                }

                showNotification(context, title, message)

                // Immediately re-schedule the next day's exact alarm
                AlarmScheduler.scheduleDailyAlarm(context, hour, minute)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "stock_reminder_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Stock Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to update garment inventory stock."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
