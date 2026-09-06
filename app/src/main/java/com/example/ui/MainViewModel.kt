package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WorkflowRepository
import com.example.data.model.EngineTelemetry
import com.example.data.model.ExecutionLog
import com.example.data.model.LiveHookEvent
import com.example.data.model.NodeConnection
import com.example.data.model.NodePort
import com.example.data.model.NodeType
import com.example.data.model.WorkflowGraph
import com.example.data.model.WorkflowNode
import com.example.engine.DeviceAPI
import com.example.engine.JsEngineRunner
import com.example.permissions.PermissionItem
import com.example.permissions.PermissionManager
import com.example.permissions.PermissionsState
import com.example.service.AutoAutomationAccessibilityService
import com.example.service.AutomationForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkflowRepository(application)
    val permissionManager = PermissionManager(application)
    val deviceAPI = DeviceAPI(application)
    val jsEngine = JsEngineRunner(application, deviceAPI)

    val graphState: StateFlow<WorkflowGraph> = repository.graphState
    val codeState: StateFlow<String> = repository.codeState
    val logsState: StateFlow<List<ExecutionLog>> = repository.logsState
    val eventsState: StateFlow<List<LiveHookEvent>> = repository.eventsState
    val telemetryState: StateFlow<EngineTelemetry> = repository.telemetryState

    private val _permissionsState = MutableStateFlow(permissionManager.checkAllPermissions())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    init {
        // Collect logs emitted from DeviceAPI and JS runtime
        viewModelScope.launch {
            DeviceAPI.logFlow.collect { log ->
                repository.appendLog(log)
            }
        }

        // Hook up accessibility events
        AutoAutomationAccessibilityService.onEventHookReceived = { type, desc ->
            repository.addHookEvent(
                LiveHookEvent(
                    id = System.currentTimeMillis().toString(),
                    title = "Hook: $type",
                    description = desc,
                    time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                    iconType = com.example.data.model.HookIconType.DEVICE
                )
            )
        }

        // Start foreground service on launch
        try {
            AutomationForegroundService.start(application)
        } catch (_: Exception) {}
    }

    fun refreshPermissions() {
        _permissionsState.value = permissionManager.checkAllPermissions()
    }

    fun grantPermission(item: PermissionItem) {
        when (item.id) {
            "accessibility" -> permissionManager.openAccessibilitySettings()
            "overlay" -> permissionManager.openOverlaySettings()
            "write_settings" -> permissionManager.openWriteSettings()
            "battery" -> permissionManager.openBatteryOptimizationSettings()
            "notification" -> permissionManager.openNotificationListenerSettings()
            else -> permissionManager.openAppSettings()
        }
    }

    fun onNodeDragged(nodeId: String, deltaX: Float, deltaY: Float) {
        val node = graphState.value.nodes.find { it.id == nodeId } ?: return
        repository.updateNodePosition(nodeId, node.x + deltaX, node.y + deltaY)
    }

    fun updateCode(newCode: String) {
        repository.updateCode(newCode)
    }

    fun syncVisualToCode() {
        repository.syncGraphToCode()
    }

    fun syncCodeToVisual() {
        repository.syncCodeToGraph()
    }

    fun toggleGraphActive(active: Boolean) {
        repository.setGraphActive(active)
    }

    fun runCurrentScript(triggerSource: String = "Manual Execution (Run Button)") {
        if (_isExecuting.value) return
        _isExecuting.value = true
        repository.incrementExecutionCount()

        jsEngine.executeScript(codeState.value, triggerSource) { success, result ->
            _isExecuting.value = false
        }
    }

    fun addTriggerNode(title: String, cron: String) {
        val id = "node_trigger_${System.currentTimeMillis()}"
        val node = WorkflowNode(
            id = id,
            title = title,
            subtitle = cron,
            type = NodeType.TRIGGER,
            x = 60f,
            y = 100f,
            badge = "TRIGGER",
            subBadge = "SCHEDULE",
            config = mapOf("cron" to cron),
            inputPorts = emptyList(),
            outputPorts = listOf(NodePort("out", "FLOW", isInput = false))
        )
        repository.addNode(node)
    }

    fun addConditionNode(title: String, ssid: String) {
        val id = "node_cond_${System.currentTimeMillis()}"
        val node = WorkflowNode(
            id = id,
            title = title,
            subtitle = "SSID Match",
            type = NodeType.CONDITION,
            x = 60f,
            y = 400f,
            badge = "LOGIC GATE",
            subBadge = "IF / ELSE",
            config = mapOf("ssid" to ssid),
            inputPorts = listOf(NodePort("in", "IN", isInput = true)),
            outputPorts = listOf(
                NodePort("false", "FALSE", isInput = false, isFalseMatch = true),
                NodePort("true", "TRUE MATCH", isInput = false, isTrueMatch = true)
            )
        )
        repository.addNode(node)
    }

    fun addActionNode(title: String, ringer: String, torch: Boolean) {
        val id = "node_act_${System.currentTimeMillis()}"
        val node = WorkflowNode(
            id = id,
            title = title,
            subtitle = "$ringer Mode",
            type = NodeType.ACTION,
            x = 60f,
            y = 740f,
            badge = "ACTION",
            subBadge = "BRIDGE OK",
            config = mapOf(
                "ringer_mode" to ringer,
                "torch_strobe" to if (torch) "2x PULSE" else "OFF"
            ),
            inputPorts = listOf(NodePort("in", "IN", isInput = true)),
            outputPorts = listOf(NodePort("out", "SUCCESS", isInput = false))
        )
        repository.addNode(node)
    }

    fun addCustomCodeNode(code: String) {
        val id = "node_custom_${System.currentTimeMillis()}"
        val node = WorkflowNode(
            id = id,
            title = "Custom Script Node",
            subtitle = "V8 Engine Block",
            type = NodeType.CUSTOM_CODE,
            x = 60f,
            y = 500f,
            badge = "SCRIPT",
            subBadge = "JS ENGINE",
            config = mapOf("code" to code),
            inputPorts = listOf(NodePort("in", "IN", isInput = true)),
            outputPorts = listOf(NodePort("out", "OUT", isInput = false))
        )
        repository.addNode(node)
    }

    fun deleteNode(nodeId: String) {
        repository.removeNode(nodeId)
    }

    fun restartEngineDaemon() {
        val ctx = getApplication<Application>()
        AutomationForegroundService.stop(ctx)
        AutomationForegroundService.start(ctx)
        deviceAPI.emitLog(com.example.data.model.LogLevel.INFO, "Engine Daemon restarted successfully")
    }

    fun clearLogs() {
        repository.clearLogs()
    }
}
