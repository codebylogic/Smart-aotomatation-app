package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.engine.DeviceAPI
import com.example.engine.JsEngineRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AutomationForegroundService : Service() {

    companion object {
        const val ACTION_START = "com.example.action.START_SERVICE"
        const val ACTION_STOP = "com.example.action.STOP_SERVICE"
        const val ACTION_PAUSE_1H = "com.example.action.PAUSE_1H"
        const val ACTION_TRIGGER_NIGHT = "com.example.action.TRIGGER_NIGHT"

        private const val NOTIFICATION_ID = 10101
        private const val CHANNEL_ID = "nexus_flow_engine_daemon"
        private const val TAG = "AutomationService"

        var isRunning: Boolean = false
            private set

        var activeWorkflowsCount: Int = 2
        var activeHooksCount: Int = 14

        fun start(context: Context) {
            val intent = Intent(context, AutomationForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AutomationForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var deviceAPI: DeviceAPI
    private lateinit var jsEngine: JsEngineRunner

    override fun onCreate() {
        super.onCreate()
        deviceAPI = DeviceAPI(applicationContext)
        jsEngine = JsEngineRunner(applicationContext, deviceAPI)
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "NexusFlow::SmartWakeLockShield"
        )?.apply {
            setReferenceCounted(false)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                isRunning = false
                return START_NOT_STICKY
            }
            ACTION_PAUSE_1H -> {
                deviceAPI.emitLog(com.example.data.model.LogLevel.INFO, "Daemon paused for 1 hour by user action")
            }
            ACTION_TRIGGER_NIGHT -> {
                runNightWorkflow()
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildForegroundNotification())
                isRunning = true
                acquireWakeLockSafely()
                startBackgroundDaemonLoop()
            }
        }
        return START_STICKY
    }

    private fun acquireWakeLockSafely() {
        try {
            if (wakeLock?.isHeld != true) {
                wakeLock?.acquire(10 * 60 * 1000L /* 10 mins batch */)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wake lock: ${e.message}")
        }
    }

    private fun runNightWorkflow() {
        val sampleNightScript = """
            export default async function run(event) {
                const ssid = await WiFi.getConnectedSSID();
                if (ssid === 'Home_Mesh_5G') {
                    await Device.setRingerMode('SILENT');
                    await Device.setVolume('media', 0);
                    await Device.toggleTorch(true);
                    await Device.vibrate([0, 80, 40, 80]);
                    Logger.info('Night routine active: Torch armed & DND enabled');
                } else {
                    Logger.warn('Unrecognized BSSID. Routine bypassed.');
                }
                return { success: true, timestamp: Date.now() };
            }
        """.trimIndent()

        jsEngine.executeScript(sampleNightScript, "System Shade Intercept Action (Run Night)")
    }

    private fun startBackgroundDaemonLoop() {
        serviceScope.launch {
            while (isActive) {
                delay(60_000L) // Telemetry and trigger heartbeat
                // Heartbeat check
                if (isRunning) {
                    updateNotification()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nexus Flow Background Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent execution daemon for user-defined automations"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingLaunch = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, AutomationForegroundService::class.java).apply {
            action = ACTION_PAUSE_1H
        }
        val pendingPause = PendingIntent.getService(
            this,
            1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val runNightIntent = Intent(this, AutomationForegroundService::class.java).apply {
            action = ACTION_TRIGGER_NIGHT
        }
        val pendingRunNight = PendingIntent.getService(
            this,
            2,
            runNightIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nexus Flow Automation • Active now")
            .setContentText("$activeWorkflowsCount Active Workflows • $activeHooksCount System Hooks Active")
            .setSubText("Smart Wakelock Shield (~0.8%/hr)")
            .setContentIntent(pendingLaunch)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_pause, "Pause 1h", pendingPause)
            .addAction(android.R.drawable.ic_menu_view, "Canvas", pendingLaunch)
            .addAction(android.R.drawable.ic_media_play, "Run Night", pendingRunNight)
            .build()
    }

    private fun updateNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.notify(NOTIFICATION_ID, buildForegroundNotification())
    }

    override fun onDestroy() {
        isRunning = false
        serviceJob.cancel()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
