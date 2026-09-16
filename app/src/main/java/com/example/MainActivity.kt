package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.HistoryScreen
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GarmentInventoryScreen
import com.example.ui.GarmentInventoryViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow


class MainActivity : ComponentActivity() {

    companion object {
        private const val REMINDER_INTERVAL_HOURS = 24L
        
        // Expose a flow so we can easily update it from NotificationSettingsDialog
        val themeModeFlow = MutableStateFlow("system")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize theme state from SharedPreferences
        val sharedPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        themeModeFlow.value = sharedPrefs.getString("theme_mode", "system") ?: "system"
        
        requestPermissionsIfNeeded()

        // Bug 1: Only schedule on first-ever app install — not unconditionally on every onCreate.
        // Unconditionally calling scheduleDailyReminder() on every launch recomputed the delay from
        // the current time, causing the reminder time to drift whenever the app was opened.
        val isFirstRun = !sharedPrefs.getBoolean("reminder_initialized", false)
        if (isFirstRun) {
            val hour = sharedPrefs.getInt("notif_hour", 20)
            val minute = sharedPrefs.getInt("notif_minute", 0)
            AlarmScheduler.scheduleDailyAlarm(this, hour, minute)
            sharedPrefs.edit().putBoolean("reminder_initialized", true).apply()
            // Clean up any legacy WorkManager tasks
            try {
                androidx.work.WorkManager.getInstance(this).cancelUniqueWork("daily_stock_reminder")
            } catch (_: Exception) {}
        }

        setContent {
            val currentThemeMode by themeModeFlow.collectAsState()
            val useDarkTheme = when (currentThemeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = useDarkTheme) {
                val viewModel: GarmentInventoryViewModel = viewModel()
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "inventory") {
                    composable("inventory") {
                        GarmentInventoryScreen(
                            viewModel = viewModel,
                            onNavigateToHistory = { navController.navigate("history") }
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // P is 28
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                101
            )
        }
    }

    fun scheduleDailyReminder() {
        val sharedPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val hour = sharedPrefs.getInt("notif_hour", 20)
        val minute = sharedPrefs.getInt("notif_minute", 0)
        AlarmScheduler.scheduleDailyAlarm(this, hour, minute)
    }
}
