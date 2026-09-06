package com.example.data.model

enum class LogLevel {
    INFO,
    DEBUG,
    SUCCESS,
    WARN,
    ERROR,
    STATUS
}

data class ExecutionLog(
    val id: String,
    val timestamp: String,
    val level: LogLevel,
    val message: String,
    val tag: String = "",
    val flowName: String = "Night Silent & Torch"
)

data class LiveHookEvent(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val iconType: HookIconType
)

enum class HookIconType {
    DEVICE,
    VOLUME,
    LOCATION,
    BLUETOOTH
}

data class EngineTelemetry(
    val isRunning: Boolean = true,
    val uptimeFormatted: String = "42h 18m 37s",
    val activeWorkflowsCount: Int = 2,
    val activeHooksCount: Int = 14,
    val batteryImpact: String = "~0.8%/hr",
    val ramMb: Float = 38.4f,
    val cpuPercent: Float = 0.2f,
    val activeTriggersCount: Int = 8,
    val executionsToday: Int = 142,
    val successRatePercent: Int = 100,
    val haltsCount: Int = 0,
    val shizukuPort: Int = 5555,
    val isSecureSettingsGranted: Boolean = true,
    val wakelockMode: String = "Smart Coalesced",
    val wakelockSync: String = "Batch sync enabled",
    val strayWakeMs: Int = 0
)
