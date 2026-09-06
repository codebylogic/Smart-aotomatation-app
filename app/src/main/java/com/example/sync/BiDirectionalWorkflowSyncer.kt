package com.example.sync

import com.example.data.model.NodeConnection
import com.example.data.model.NodePort
import com.example.data.model.NodeType
import com.example.data.model.WorkflowGraph
import com.example.data.model.WorkflowNode

object BiDirectionalWorkflowSyncer {

    fun generateJsFromGraph(graph: WorkflowGraph): String {
        val triggerNode = graph.nodes.firstOrNull { it.type == NodeType.TRIGGER }
        val condNode = graph.nodes.firstOrNull { it.type == NodeType.CONDITION }
        val actionNode = graph.nodes.firstOrNull { it.type == NodeType.ACTION }
        val customNode = graph.nodes.firstOrNull { it.type == NodeType.CUSTOM_CODE }

        val sb = StringBuilder()
        sb.append("// Dual-Engine Android System Orchestrator\n")
        sb.append("import { Device, WiFi, System, Logger } from '@nexus/engine';\n\n")
        sb.append("export default async function run(event) {\n")

        if (customNode != null) {
            val customCode = customNode.config["code"] ?: "// Custom script block\nawait Device.toggleTorch(true);\n"
            customCode.lines().forEach { line ->
                sb.append("  ").append(line).append("\n")
            }
        } else if (condNode != null) {
            val ssid = condNode.config["ssid"] ?: "Home_Mesh_5G"
            val ringer = actionNode?.config?.get("ringer_mode") ?: "SILENT"
            val vol = actionNode?.config?.get("media_vol") ?: "0"

            sb.append("  const ssid = await WiFi.getConnectedSSID();\n")
            sb.append("  if (ssid === '$ssid') {\n")
            sb.append("    await Device.setRingerMode('$ringer');\n")
            sb.append("    await Device.setVolume('media', $vol);\n")
            sb.append("    await Device.toggleTorch(true);\n")
            sb.append("    await Device.vibrate([0, 80, 40, 80]);\n")
            sb.append("    Logger.info('Night routine active: Torch armed & DND enabled');\n")
            sb.append("  } else {\n")
            sb.append("    Logger.warn('Unrecognized BSSID. Routine bypassed.');\n")
            sb.append("  }\n")
        } else if (actionNode != null) {
            val ringer = actionNode.config["ringer_mode"] ?: "SILENT"
            sb.append("  await Device.setRingerMode('$ringer');\n")
            sb.append("  await Device.toggleTorch(true);\n")
            sb.append("  Logger.info('Action executed directly without conditions');\n")
        } else {
            sb.append("  Logger.info('Default workflow execution tick');\n")
        }

        sb.append("  return { success: true, timestamp: Date.now() };\n")
        sb.append("}\n\n")

        val cron = triggerNode?.config?.get("cron") ?: "0 22 * * *"
        sb.append("// Hook: BroadcastReceiver('android.intent.action.TIME_TICK') [cron: $cron]\n\n")
        sb.append("// End of execution graph payload\n")

        return sb.toString()
    }

    fun parseJsToGraph(jsCode: String, previousGraph: WorkflowGraph): WorkflowGraph {
        val newNodes = mutableListOf<WorkflowNode>()
        val newConnections = mutableListOf<NodeConnection>()

        // 1. Check for Trigger
        var cronStr = "0 22 * * *"
        val cronMatch = Regex("""\[cron:\s*([^\]]+)\]""").find(jsCode)
        if (cronMatch != null) {
            cronStr = cronMatch.groupValues[1].trim()
        }

        val triggerNode = WorkflowNode(
            id = "node_trigger_time",
            title = "Time Trigger",
            subtitle = if (cronStr.contains("22")) "Daily @ 10:00 PM" else "Custom Schedule",
            type = NodeType.TRIGGER,
            x = 60f,
            y = 80f,
            badge = "TRIGGER",
            subBadge = "CRON",
            config = mapOf("cron" to cronStr, "interval" to "Active Trigger"),
            inputPorts = emptyList(),
            outputPorts = listOf(NodePort("out", "FLOW", isInput = false))
        )
        newNodes.add(triggerNode)

        // 2. Check for Wi-Fi condition: if (ssid === '...')
        val ssidMatch = Regex("""ssid\s*===?\s*['"]([^'"]+)['"]""").find(jsCode)
        var conditionAdded = false
        if (ssidMatch != null) {
            val ssidValue = ssidMatch.groupValues[1]
            val condNode = WorkflowNode(
                id = "node_wifi_cond",
                title = "Wi-Fi Condition",
                subtitle = "SSID Check",
                type = NodeType.CONDITION,
                x = 60f,
                y = 380f,
                badge = "LOGIC GATE",
                subBadge = "IF / ELSE",
                config = mapOf("operator" to "==", "ssid" to ssidValue),
                inputPorts = listOf(NodePort("in", "IN", isInput = true)),
                outputPorts = listOf(
                    NodePort("false", "FALSE", isInput = false, isFalseMatch = true),
                    NodePort("true", "TRUE MATCH", isInput = false, isTrueMatch = true)
                )
            )
            newNodes.add(condNode)
            newConnections.add(NodeConnection("conn_1_2", triggerNode.id, "out", condNode.id, "in"))
            conditionAdded = true
        }

        // 3. Check for Device actions
        val hasRinger = jsCode.contains("Device.setRingerMode")
        val hasTorch = jsCode.contains("Device.toggleTorch")
        val hasVolume = jsCode.contains("Device.setVolume")

        if (hasRinger || hasTorch || hasVolume) {
            val ringerMatch = Regex("""Device\.setRingerMode\(['"]([^'"]+)['"]\)""").find(jsCode)
            val ringerVal = ringerMatch?.groupValues?.get(1) ?: "SILENT"

            val actionNode = WorkflowNode(
                id = "node_device_state",
                title = "Device State",
                subtitle = "Silent Mode & Flash",
                type = NodeType.ACTION,
                x = 60f,
                y = if (conditionAdded) 720f else 380f,
                badge = "ACTION",
                subBadge = "BRIDGE OK",
                config = mapOf(
                    "ringer_mode" to ringerVal,
                    "torch_strobe" to if (hasTorch) "2x PULSE" else "OFF",
                    "media_vol" to "0"
                ),
                inputPorts = listOf(NodePort("in", "IN", isInput = true)),
                outputPorts = listOf(NodePort("out", "SUCCESS", isInput = false))
            )
            newNodes.add(actionNode)

            if (conditionAdded) {
                newConnections.add(NodeConnection("conn_2_3", "node_wifi_cond", "true", actionNode.id, "in"))
            } else {
                newConnections.add(NodeConnection("conn_1_3", triggerNode.id, "out", actionNode.id, "in"))
            }
        } else if (!conditionAdded && jsCode.trim().length > 100) {
            // Non-parseable complex code wrapped inside "Custom Script Node"
            val customCodeNode = WorkflowNode(
                id = "node_custom_script",
                title = "Custom Script Node",
                subtitle = "Raw JavaScript Block",
                type = NodeType.CUSTOM_CODE,
                x = 60f,
                y = 380f,
                badge = "CUSTOM SCRIPT",
                subBadge = "V8/QUICKJS",
                config = mapOf("code" to jsCode),
                inputPorts = listOf(NodePort("in", "IN", isInput = true)),
                outputPorts = listOf(NodePort("out", "OUT", isInput = false))
            )
            newNodes.add(customCodeNode)
            newConnections.add(NodeConnection("conn_custom", triggerNode.id, "out", customCodeNode.id, "in"))
        }

        return previousGraph.copy(
            nodes = if (newNodes.isNotEmpty()) newNodes else previousGraph.nodes,
            connections = if (newConnections.isNotEmpty()) newConnections else previousGraph.connections
        )
    }
}
