package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.service.AutomationForegroundService

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.i(TAG, "Device booted. Starting NexusFlow Automation Daemon...")
            try {
                AutomationForegroundService.start(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service on boot: ${e.message}")
            }
        }
    }
}
