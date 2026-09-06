package com.example.data

import android.content.Context
import com.example.data.model.EngineTelemetry
import com.example.data.model.ExecutionLog
import com.example.data.model.HookIconType
import com.example.data.model.LiveHookEvent
import com.example.data.model.LogLevel
import com.example.data.model.WorkflowGraph
import com.example.data.model.WorkflowNode
import com.example.sync.BiDirectionalWorkflowSyncer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkflowRepository(private val context: Context) {

    private val _graphState = MutableStateFlow(WorkflowGraph.createDefault())
    val graphState: StateFlow<WorkflowGraph> = _graphState.asStateFlow()

    private val defaultJs = BiDirectionalWorkflowSyncer.generateJsFromGraph(_graphState.value)
    private val _codeState = MutableStateFlow(defaultJs)
    val codeState: StateFlow<String> = _codeState.asStateFlow()

    private val initialLogs = listOf(
        ExecutionLog("1", "[10:00:00.124]", LogLevel.INFO, "Triggered via AccessibilityService hook\n(Gesture: DOUBLE_TAP_BACK)"),
        ExecutionLog("2", "[10:00:00.342]", LogLevel.DEBUG, "WiFi.getConnectedSSID() -> 'Home_Mesh_5G'\n[BSSID: 3a:8f:c2:00:19]"),
        ExecutionLog("3", "[10:00:01.005]", LogLevel.SUCCESS, "Device.setRingerMode('SILENT') applied successfully"),
        ExecutionLog("4", "[10:00:01.189]", LogLevel.SUCCESS, "Torch activated (PWM Intensity: 100%, LED_ID_REAR)"),
        ExecutionLog("5", "[10:00:01.201]", LogLevel.STATUS, "Process finished with exit code 0 (Elapsed: 12ms)")
    )
    private val _logsState = MutableStateFlow<List<ExecutionLog>>(initialLogs)
    val logsState: StateFlow<List<ExecutionLog>> = _logsState.asStateFlow()

    private val initialEvents = listOf(
        LiveHookEvent("ev1", "android.intent.action.SCREEN_OFF", "Dispatched payload to PowerPolicyNode", "22:00:14", HookIconType.DEVICE),
        LiveHookEvent("ev2", "KeyHook: DOUBLE_CLICK (VOL_D...", "Triggered: Flashlight Toggle Flow", "21:58:02", HookIconType.VOLUME),
        LiveHookEvent("ev3", "Geofence Exit: HOME_ZONE (Radi...", "Switched WiFi off, enabled Cellular Guard", "21:42:19", HookIconType.LOCATION),
        LiveHookEvent("ev4", "BT_DEVICE: OBD2_SCANNER_v4", "Car Telemetry Flow Hook attached", "21:15:00", HookIconType.BLUETOOTH)
    )
    private val _eventsState = MutableStateFlow<List<LiveHookEvent>>(initialEvents)
    val eventsState: StateFlow<List<LiveHookEvent>> = _eventsState.asStateFlow()

    private val _telemetryState = MutableStateFlow(EngineTelemetry())
    val telemetryState: StateFlow<EngineTelemetry> = _telemetryState.asStateFlow()

    fun updateNodePosition(nodeId: String, newX: Float, newY: Float) {
        val current = _graphState.value
        val updatedNodes = current.nodes.map { node ->
            if (node.id == nodeId) node.copy(x = newX, y = newY) else node
        }
        _graphState.value = current.copy(nodes = updatedNodes)
    }

    fun addNode(node: WorkflowNode) {
        val current = _graphState.value
        _graphState.value = current.copy(nodes = current.nodes + node)
        syncGraphToCode()
    }

    fun removeNode(nodeId: String) {
        val current = _graphState.value
        val updatedNodes = current.nodes.filterNot { it.id == nodeId }
        val updatedConns = current.connections.filterNot { it.fromNodeId == nodeId || it.toNodeId == nodeId }
        _graphState.value = current.copy(nodes = updatedNodes, connections = updatedConns)
        syncGraphToCode()
    }

    fun setGraphActive(active: Boolean) {
        _graphState.value = _graphState.value.copy(isActive = active)
    }

    fun updateCode(code: String) {
        _codeState.value = code
    }

    fun syncGraphToCode() {
        val generated = BiDirectionalWorkflowSyncer.generateJsFromGraph(_graphState.value)
        _codeState.value = generated
    }

    fun syncCodeToGraph() {
        val parsed = BiDirectionalWorkflowSyncer.parseJsToGraph(_codeState.value, _graphState.value)
        _graphState.value = parsed
    }

    fun appendLog(log: ExecutionLog) {
        val current = _logsState.value.toMutableList()
        current.add(0, log) // prepend newest
        if (current.size > 200) {
            _logsState.value = current.take(200)
        } else {
            _logsState.value = current
        }
    }

    fun clearLogs() {
        _logsState.value = emptyList()
    }

    fun addHookEvent(event: LiveHookEvent) {
        val current = _eventsState.value.toMutableList()
        current.add(0, event)
        _eventsState.value = current.take(50)
    }

    fun incrementExecutionCount() {
        val t = _telemetryState.value
        _telemetryState.value = t.copy(executionsToday = t.executionsToday + 1)
    }
}
