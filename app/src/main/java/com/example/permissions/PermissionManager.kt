package com.example.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.service.AutoAutomationAccessibilityService

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val isSpecialAccess: Boolean,
    val actionLabel: String = "Grant Access"
)

data class PermissionsState(
    val items: List<PermissionItem> = emptyList(),
    val grantedCount: Int = 4,
    val totalCount: Int = 7
) {
    val percentage: Int get() = if (totalCount > 0) (grantedCount * 100) / totalCount else 0
}

class PermissionManager(private val context: Context) {

    fun checkAllPermissions(): PermissionsState {
        val accessibilityGranted = AutoAutomationAccessibilityService.isServiceRunning()

        val overlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

        val writeSettingsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }

        val batteryOptimizationsIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } else {
            true
        }

        val locationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val cameraGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val notificationListenerGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: ""
            enabledListeners.contains(context.packageName)
        } else {
            false
        }

        val items = listOf(
            PermissionItem(
                id = "accessibility",
                title = "Accessibility Service",
                description = "Monitors UI gestures, app launches, and simulates screen interaction.",
                isGranted = accessibilityGranted,
                isSpecialAccess = true
            ),
            PermissionItem(
                id = "overlay",
                title = "Draw Over Apps",
                description = "Required to display floating action widgets and HUD automation triggers.",
                isGranted = overlayGranted,
                isSpecialAccess = true
            ),
            PermissionItem(
                id = "write_settings",
                title = "Modify System Settings",
                description = "Allows changing system volumes, display timeout, and dark mode automatically.",
                isGranted = writeSettingsGranted,
                isSpecialAccess = true,
                actionLabel = "Grant Access"
            ),
            PermissionItem(
                id = "battery",
                title = "Ignore Battery Optimizations",
                description = "Prevents Android OS from killing the automation runtime in deep sleep.",
                isGranted = batteryOptimizationsIgnored,
                isSpecialAccess = true
            ),
            PermissionItem(
                id = "geofence",
                title = "Background Geofencing",
                description = "Triggers smart home flows based on precise Geofence entry/exit.",
                isGranted = locationGranted,
                isSpecialAccess = false,
                actionLabel = "Allow All Time"
            ),
            PermissionItem(
                id = "camera",
                title = "Camera & Flashlight Control",
                description = "Controls LED torch gestures and background photo triggers.",
                isGranted = cameraGranted,
                isSpecialAccess = false
            ),
            PermissionItem(
                id = "notification",
                title = "Notification Listener",
                description = "Intercepts incoming alerts for automated parsing and SMS dispatch.",
                isGranted = notificationListenerGranted,
                isSpecialAccess = false,
                actionLabel = "Bind Listener"
            )
        )

        val granted = items.count { it.isGranted }
        return PermissionsState(
            items = items,
            grantedCount = granted,
            totalCount = items.size
        )
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openBatteryOptimizationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
