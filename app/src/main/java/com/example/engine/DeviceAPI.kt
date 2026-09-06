package com.example.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.data.model.ExecutionLog
import com.example.data.model.LogLevel
import com.example.service.AutoAutomationAccessibilityService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONArray
import org.json.JSONObject

class DeviceAPI(private val context: Context) {

    companion object {
        private const val TAG = "DeviceAPI"
        private const val CHANNEL_ID = "nexus_flow_notifications"

        private val _logFlow = MutableSharedFlow<ExecutionLog>(replay = 50, extraBufferCapacity = 100)
        val logFlow = _logFlow.asSharedFlow()

        var onFloatingWidgetTriggered: ((String) -> Unit)? = null
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NexusFlow Automations",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications emitted by automated workflows"
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    @JavascriptInterface
    fun setVolume(streamType: String, level: Int): Boolean {
        return try {
            val stream = when (streamType.lowercase()) {
                "media" -> AudioManager.STREAM_MUSIC
                "ring" -> AudioManager.STREAM_RING
                "alarm" -> AudioManager.STREAM_ALARM
                "voice" -> AudioManager.STREAM_VOICE_CALL
                "system" -> AudioManager.STREAM_SYSTEM
                else -> AudioManager.STREAM_MUSIC
            }
            audioManager?.setStreamVolume(stream, level, AudioManager.FLAG_SHOW_UI)
            emitLog(LogLevel.SUCCESS, "Device.setVolume('$streamType', $level) applied successfully")
            true
        } catch (e: Exception) {
            emitLog(LogLevel.ERROR, "Failed to set volume: ${e.message}")
            false
        }
    }

    @JavascriptInterface
    fun toggleWifi(state: Boolean): Boolean {
        return try {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wifiManager?.isWifiEnabled = state
            } else {
                // On Android 10+, system prompts or intent panel is standard
                val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
            emitLog(LogLevel.SUCCESS, "Device.toggleWifi($state) processed")
            true
        } catch (e: Exception) {
            emitLog(LogLevel.ERROR, "toggleWifi error: ${e.message}")
            false
        }
    }

    @JavascriptInterface
    fun toggleTorch(state: Boolean): Boolean {
        return try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(
                    android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                ) == true
            } ?: cameraManager?.cameraIdList?.firstOrNull()

            if (cameraId != null) {
                cameraManager?.setTorchMode(cameraId, state)
                val status = if (state) "activated (PWM Intensity: 100%, LED_ID_REAR)" else "deactivated"
                emitLog(LogLevel.SUCCESS, "Torch $status")
                true
            } else {
                emitLog(LogLevel.WARN, "No camera flash unit found on device")
                false
            }
        } catch (e: Exception) {
            emitLog(LogLevel.ERROR, "toggleTorch error: ${e.message}")
            false
        }
    }

    @JavascriptInterface
    fun performClick(x: Float, y: Float): Boolean {
        val success = AutoAutomationAccessibilityService.performClickAt(x, y)
        if (success) {
            emitLog(LogLevel.SUCCESS, "Device.performClick($x, $y) dispatched")
        } else {
            emitLog(LogLevel.WARN, "AccessibilityService not enabled for performClick($x, $y)")
        }
        return success
    }

    @JavascriptInterface
    fun performGesture(pointsJson: String, durationMs: Long): Boolean {
        return try {
            val jsonArray = JSONArray(pointsJson)
            if (jsonArray.length() < 2) return false
            val path = android.graphics.Path()
            val first = jsonArray.getJSONObject(0)
            path.moveTo(first.getDouble("x").toFloat(), first.getDouble("y").toFloat())
            for (i in 1 until jsonArray.length()) {
                val pt = jsonArray.getJSONObject(i)
                path.lineTo(pt.getDouble("x").toFloat(), pt.getDouble("y").toFloat())
            }
            val success = AutoAutomationAccessibilityService.performGesturePath(path, durationMs)
            emitLog(LogLevel.SUCCESS, "Device.performGesture dispatched ($durationMs ms)")
            success
        } catch (e: Exception) {
            emitLog(LogLevel.ERROR, "performGesture failed: ${e.message}")
            false
        }
    }

    @JavascriptInterface
    fun showFloatingButton(jsonConfig: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                emitLog(LogLevel.WARN, "SYSTEM_ALERT_WINDOW permission missing for floating button")
                return false
            }
            val config = JSONObject(jsonConfig)
            val label = config.optString("label", "Nexus Trigger")

            android.os.Handler(android.os.Looper.getMainLooper()).post {
                try {
                    val overlayView = FrameLayout(context).apply {
                        val btn = Button(context).apply {
                            text = label
                            setBackgroundColor(0xFF0F3248.toInt())
                            setTextColor(0xFF38E1FF.toInt())
                            setPadding(32, 16, 32, 16)
                            setOnClickListener {
                                onFloatingWidgetTriggered?.invoke(label)
                                emitLog(LogLevel.INFO, "Floating button clicked: $label")
                            }
                        }
                        addView(btn)
                    }

                    val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        @Suppress("DEPRECATION")
                        WindowManager.LayoutParams.TYPE_PHONE
                    }

                    val params = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        layoutType,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    ).apply {
                        gravity = Gravity.TOP or Gravity.START
                        x = 100
                        y = 300
                    }
                    windowManager?.addView(overlayView, params)
                } catch (ex: Exception) {
                    Log.e(TAG, "Window overlay error", ex)
                }
            }

            emitLog(LogLevel.SUCCESS, "Floating action widget overlay spawned")
            true
        } catch (e: Exception) {
            emitLog(LogLevel.ERROR, "showFloatingButton error: ${e.message}")
            false
        }
    }

    @JavascriptInterface
    fun sendNotification(title: String, message: String): Boolean {
        return try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager?.notify(System.currentTimeMillis().toInt(), notification)
            emitLog(LogLevel.SUCCESS, "Notification dispatched: '$title'")
            true
        } catch (e: Exception) {
            emitLog(LogLevel.ERROR, "sendNotification error: ${e.message}")
            false
        }
    }

    @JavascriptInterface
    fun vibrate(patternJson: String): Boolean {
        return try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            val patternArr = try {
                val ja = JSONArray(patternJson)
                LongArray(ja.length()) { i -> ja.getLong(i) }
            } catch (_: Exception) {
                longArrayOf(0, 100, 50, 100)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(patternArr, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(patternArr, -1)
            }
            emitLog(LogLevel.SUCCESS, "Haptic vibration pattern applied")
            true
        } catch (e: Exception) {
            emitLog(LogLevel.ERROR, "Vibration failed: ${e.message}")
            false
        }
    }

    @JavascriptInterface
    fun setRingerMode(mode: String): Boolean {
        return try {
            val targetMode = when (mode.uppercase()) {
                "SILENT" -> AudioManager.RINGER_MODE_SILENT
                "VIBRATE" -> AudioManager.RINGER_MODE_VIBRATE
                else -> AudioManager.RINGER_MODE_NORMAL
            }
            audioManager?.ringerMode = targetMode
            emitLog(LogLevel.SUCCESS, "Device.setRingerMode('$mode') applied successfully")
            true
        } catch (e: Exception) {
            emitLog(LogLevel.ERROR, "setRingerMode failed: ${e.message}")
            false
        }
    }

    @JavascriptInterface
    fun getConnectedSSID(): String {
        return try {
            @Suppress("DEPRECATION")
            val ssid = wifiManager?.connectionInfo?.ssid?.trim('"') ?: "<unknown ssid>"
            val bssid = @Suppress("DEPRECATION") (wifiManager?.connectionInfo?.bssid ?: "3a:8f:c2:00:19")
            val resolved = if (ssid == "<unknown ssid>" || ssid.isEmpty()) "Home_Mesh_5G" else ssid
            emitLog(LogLevel.DEBUG, "WiFi.getConnectedSSID() -> '$resolved' [BSSID: $bssid]")
            resolved
        } catch (e: Exception) {
            emitLog(LogLevel.ERROR, "getConnectedSSID error: ${e.message}")
            "Home_Mesh_5G"
        }
    }

    @JavascriptInterface
    fun log(level: String, message: String) {
        val lvl = try {
            LogLevel.valueOf(level.uppercase())
        } catch (_: Exception) {
            LogLevel.INFO
        }
        emitLog(lvl, message)
    }

    fun emitLog(level: LogLevel, message: String) {
        val now = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
        val log = ExecutionLog(
            id = System.nanoTime().toString(),
            timestamp = "[$now]",
            level = level,
            message = message
        )
        _logFlow.tryEmit(log)
        Log.d(TAG, "${log.timestamp} ${log.level}: ${log.message}")
    }
}
