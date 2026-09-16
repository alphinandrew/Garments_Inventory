package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Bug 1: AlarmManager alarms do not survive device reboot.
// BootReceiver re-registers the daily alarm when the device boots.
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val hour = sharedPrefs.getInt("notif_hour", 20)
            val minute = sharedPrefs.getInt("notif_minute", 0)
            AlarmScheduler.scheduleDailyAlarm(context, hour, minute)
        }
    }
}
