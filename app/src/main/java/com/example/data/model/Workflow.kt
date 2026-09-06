package com.example.data.model

enum class NodeType {
    TRIGGER,
    CONDITION,
    ACTION,
    CUSTOM_CODE
}

data class NodePort(
    val id: String,
    val label: String,
    val isInput: Boolean,
    val isTrueMatch: Boolean = false,
    val isFalseMatch: Boolean = false
)

data class WorkflowNode(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: NodeType,
    var x: Float,
    var y: Float,
    val badge: String = "",
    val subBadge: String = "",
    val config: Map<String, String> = emptyMap(),
    val inputPorts: List<NodePort> = listOf(NodePort("in", "IN", true)),
    val outputPorts: List<NodePort> = listOf(NodePort("out", "OUT", false))
)

data class NodeConnection(
    val id: String,
    val fromNodeId: String,
    val fromPortId: String,
    val toNodeId: String,
    val toPortId: String
)

data class WorkflowGraph(
    val id: String = "night_silent_torch",
    val name: String = "Night Silent & Torch",
    val isActive: Boolean = true,
    val nodes: List<WorkflowNode> = emptyList(),
    val connections: List<NodeConnection> = emptyList()
) {
    companion object {
        fun createDefault(): WorkflowGraph {
            val node1 = WorkflowNode(
                id = "node_trigger_time",
                title = "Time Trigger",
                subtitle = "Daily @ 10:00 PM",
                type = NodeType.TRIGGER,
                x = 60f,
                y = 80f,
                badge = "TRIGGER",
                subBadge = "CRON",
                config = mapOf(
                    "cron" to "0 22 * * *",
                    "interval" to "Daily @ 10:00 PM"
                ),
                inputPorts = emptyList(),
                outputPorts = listOf(
                    NodePort(id = "out", label = "FLOW", isInput = false)
                )
            )

            val node2 = WorkflowNode(
                id = "node_wifi_cond",
                title = "Wi-Fi Condition",
                subtitle = "SSID Check",
                type = NodeType.CONDITION,
                x = 60f,
                y = 380f,
                badge = "LOGIC GATE",
                subBadge = "IF / ELSE",
                config = mapOf(
                    "operator" to "==",
                    "ssid" to "Home_Mesh_5G"
                ),
                inputPorts = listOf(
                    NodePort(id = "in", label = "IN", isInput = true)
                ),
                outputPorts = listOf(
                    NodePort(id = "false", label = "FALSE", isInput = false, isFalseMatch = true),
                    NodePort(id = "true", label = "TRUE MATCH", isInput = false, isTrueMatch = true)
                )
            )

            val node3 = WorkflowNode(
                id = "node_device_state",
                title = "Device State",
                subtitle = "Silent Mode & Flash",
                type = NodeType.ACTION,
                x = 60f,
                y = 720f,
                badge = "ACTION",
                subBadge = "BRIDGE OK",
                config = mapOf(
                    "ringer_mode" to "SILENT",
                    "torch_strobe" to "2x PULSE",
                    "media_vol" to "0"
                ),
                inputPorts = listOf(
                    NodePort(id = "in", label = "IN", isInput = true)
                ),
                outputPorts = listOf(
                    NodePort(id = "out", label = "SUCCESS", isInput = false)
                )
            )

            val connections = listOf(
                NodeConnection("conn_1_2", "node_trigger_time", "out", "node_wifi_cond", "in"),
                NodeConnection("conn_2_3", "node_wifi_cond", "true", "node_device_state", "in")
            )

            return WorkflowGraph(
                id = "night_silent_torch",
                name = "Night Silent & Torch",
                isActive = true,
                nodes = listOf(node1, node2, node3),
                connections = connections
            )
        }
    }
}
