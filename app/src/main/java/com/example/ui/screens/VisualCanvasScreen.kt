package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NodeType
import com.example.data.model.WorkflowNode
import com.example.ui.MainViewModel
import com.example.ui.components.NexusTopBar
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import kotlin.math.roundToInt

@Composable
fun VisualCanvasScreen(
    viewModel: MainViewModel,
    onNavigateToCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val graph by viewModel.graphState.collectAsState()
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    var showAddNodeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("visual_canvas_screen")
    ) {
        NexusTopBar(
            title = "Canvas",
            onCpuClick = { viewModel.restartEngineDaemon() },
            onProfileClick = {}
        )

        // Subheader with Workflow status and action controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CyberCyan)
                )
                Text(
                    text = "LIVE RUNTIME",
                    color = CyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = graph.name,
                    color = CyberTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (graph.isActive) "ACTIVE" else "PAUSED",
                    color = if (graph.isActive) CyberCyan else CyberTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = graph.isActive,
                    onCheckedChange = { viewModel.toggleGraphActive(it) },
                    modifier = Modifier.testTag("canvas_active_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF04101A),
                        checkedTrackColor = CyberCyan,
                        uncheckedTrackColor = CyberCard
                    )
                )

                IconButton(
                    onClick = { viewModel.runCurrentScript("Canvas Run Button") },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CyberCard)
                        .border(1.dp, CyberCyan.copy(alpha = 0.5f), CircleShape)
                        .testTag("canvas_run_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run Workflow",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.syncVisualToCode()
                        onNavigateToCode()
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CyberCard)
                        .border(1.dp, CyberBorder, CircleShape)
                        .testTag("canvas_view_code_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "View Code Editor",
                        tint = CyberTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Infinite / Pan-Zoom Canvas area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color(0xFF060910))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.6f, 2.0f)
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
        ) {
            // Background grid dots
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotSpacing = 28.dp.toPx() * zoomScale
                val dotRadius = 1.5.dp.toPx()
                val startX = (panOffsetX % dotSpacing + dotSpacing) % dotSpacing
                val startY = (panOffsetY % dotSpacing + dotSpacing) % dotSpacing

                var x = startX
                while (x < size.width) {
                    var y = startY
                    while (y < size.height) {
                        drawCircle(
                            color = Color(0xFF192438),
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                        y += dotSpacing
                    }
                    x += dotSpacing
                }
            }

            // Connection Bezier Curves
            Canvas(modifier = Modifier.fillMaxSize()) {
                graph.connections.forEach { conn ->
                    val fromNode = graph.nodes.find { it.id == conn.fromNodeId }
                    val toNode = graph.nodes.find { it.id == conn.toNodeId }

                    if (fromNode != null && toNode != null) {
                        // Calculate port center positions based on node coordinates
                        val startX = (fromNode.x + 280f) * zoomScale + panOffsetX
                        val startY = (fromNode.y + if (conn.fromPortId == "true") 150f else 90f) * zoomScale + panOffsetY
                        val endX = toNode.x * zoomScale + panOffsetX
                        val endY = (toNode.y + 90f) * zoomScale + panOffsetY

                        val curvePath = Path().apply {
                            moveTo(startX, startY)
                            val controlX1 = startX + 80f * zoomScale
                            val controlY1 = startY
                            val controlX2 = endX - 80f * zoomScale
                            val controlY2 = endY
                            cubicTo(controlX1, controlY1, controlX2, controlY2, endX, endY)
                        }

                        val strokeColor = when (fromNode.type) {
                            NodeType.TRIGGER -> CyberAmber
                            NodeType.CONDITION -> if (conn.fromPortId == "true") CyberGreen else CyberRed
                            NodeType.ACTION -> CyberGreen
                            NodeType.CUSTOM_CODE -> CyberPurple
                        }

                        // Glow stroke
                        drawPath(
                            path = curvePath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(strokeColor.copy(alpha = 0.8f), CyberCyan.copy(alpha = 0.8f)),
                                startX = startX,
                                endX = endX
                            ),
                            style = Stroke(width = 3.5.dp.toPx() * zoomScale, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // Render Nodes
            graph.nodes.forEach { node ->
                val renderedX = (node.x * zoomScale + panOffsetX).roundToInt()
                val renderedY = (node.y * zoomScale + panOffsetY).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(renderedX, renderedY) }
                        .pointerInput(node.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                viewModel.onNodeDragged(
                                    node.id,
                                    dragAmount.x / zoomScale,
                                    dragAmount.y / zoomScale
                                )
                            }
                        }
                ) {
                    WorkflowNodeCard(
                        node = node,
                        onDelete = { viewModel.deleteNode(node.id) }
                    )
                }
            }

            // Floating + Add Node button on Bottom Left
            Button(
                onClick = { showAddNodeDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .testTag("canvas_add_node_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = Color(0xFF04101A)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Node", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Floating Canvas Zoom Controller on Bottom Right
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardElevated.copy(alpha = 0.95f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            zoomScale = 1.0f
                            panOffsetX = 0f
                            panOffsetY = 0f
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.CropFree, contentDescription = "Fit View", tint = CyberTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.0f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = CyberTextPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.6f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = CyberTextPrimary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${(zoomScale * 100).toInt()}%",
                        color = CyberTextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showAddNodeDialog) {
        AddNodeDialog(
            onDismiss = { showAddNodeDialog = false },
            onAddTrigger = { title, cron ->
                viewModel.addTriggerNode(title, cron)
                showAddNodeDialog = false
            },
            onAddCondition = { title, ssid ->
                viewModel.addConditionNode(title, ssid)
                showAddNodeDialog = false
            },
            onAddAction = { title, ringer, torch ->
                viewModel.addActionNode(title, ringer, torch)
                showAddNodeDialog = false
            },
            onAddCustomCode = { code ->
                viewModel.addCustomCodeNode(code)
                showAddNodeDialog = false
            }
        )
    }
}

@Composable
fun WorkflowNodeCard(
    node: WorkflowNode,
    onDelete: () -> Unit
) {
    val accentColor = when (node.type) {
        NodeType.TRIGGER -> CyberAmber
        NodeType.CONDITION -> CyberBlue
        NodeType.ACTION -> CyberGreen
        NodeType.CUSTOM_CODE -> CyberPurple
    }

    Box(
        modifier = Modifier
            .width(290.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CyberCard)
            .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
            .testTag("node_card_${node.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top colored glowing bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(accentColor)
            )

            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = node.badge,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = node.subBadge,
                        color = CyberTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Body Section with Icon & Titles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCardElevated)
                        .border(1.dp, CyberBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (node.type) {
                        NodeType.TRIGGER -> Icons.Default.AccessTime
                        NodeType.CONDITION -> Icons.Default.Wifi
                        NodeType.ACTION -> Icons.Default.NightlightRound
                        NodeType.CUSTOM_CODE -> Icons.Default.Code
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = node.title,
                        color = CyberTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = node.subtitle,
                        color = CyberTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Config detail box depending on node type
            when (node.type) {
                NodeType.TRIGGER -> {
                    val cron = node.config["cron"] ?: "0 22 * * *"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF090D17))
                            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🔁 $cron",
                            color = CyberTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
                NodeType.CONDITION -> {
                    val ssid = node.config["ssid"] ?: "Home_Mesh_5G"
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF090D17))
                                .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "== '$ssid'",
                                    color = CyberCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CyberGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("FALSE", color = CyberRed.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("TRUE MATCH", color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                NodeType.ACTION -> {
                    val ringer = node.config["ringer_mode"] ?: "SILENT"
                    val strobe = node.config["torch_strobe"] ?: "2x PULSE"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF090D17))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("DND / Ringer Mode", color = CyberTextSecondary, fontSize = 11.sp)
                            Text(ringer, color = CyberGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF090D17))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Torch Strobe", color = CyberTextSecondary, fontSize = 11.sp)
                            Text(strobe, color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                NodeType.CUSTOM_CODE -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF090D17))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = node.config["code"] ?: "// Custom script block",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 3
                        )
                    }
                }
            }
        }

        // Port indicators on edges
        if (node.inputPorts.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-8).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(CyberBlue)
                    .border(2.dp, CyberBackground, CircleShape)
            )
        }

        if (node.outputPorts.isNotEmpty()) {
            val portColor = if (node.type == NodeType.TRIGGER) CyberAmber else CyberGreen
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 8.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(portColor)
                    .border(2.dp, CyberBackground, CircleShape)
            )
        }
    }
}

@Composable
fun AddNodeDialog(
    onDismiss: () -> Unit,
    onAddTrigger: (String, String) -> Unit,
    onAddCondition: (String, String) -> Unit,
    onAddAction: (String, String, Boolean) -> Unit,
    onAddCustomCode: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Trigger") }
    var title by remember { mutableStateOf("Schedule Trigger") }
    var param1 by remember { mutableStateOf("0 22 * * *") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Workflow Node", color = CyberTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Trigger", "Condition", "Action", "Code").forEach { cat ->
                        Button(
                            onClick = {
                                selectedCategory = cat
                                when (cat) {
                                    "Trigger" -> {
                                        title = "Time Trigger"
                                        param1 = "0 22 * * *"
                                    }
                                    "Condition" -> {
                                        title = "Wi-Fi Condition"
                                        param1 = "Home_Mesh_5G"
                                    }
                                    "Action" -> {
                                        title = "Device State"
                                        param1 = "SILENT"
                                    }
                                    "Code" -> {
                                        title = "Custom JS Script"
                                        param1 = "await Device.toggleTorch(true);"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedCategory == cat) CyberCyan else CyberCardElevated,
                                contentColor = if (selectedCategory == cat) Color(0xFF04101A) else CyberTextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(cat, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Node Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = param1,
                    onValueChange = { param1 = it },
                    label = {
                        Text(
                            when (selectedCategory) {
                                "Trigger" -> "Cron Expression"
                                "Condition" -> "Expected Wi-Fi SSID"
                                "Action" -> "Ringer Mode (SILENT / NORMAL)"
                                else -> "JavaScript Code"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedCategory) {
                        "Trigger" -> onAddTrigger(title, param1)
                        "Condition" -> onAddCondition(title, param1)
                        "Action" -> onAddAction(title, param1, true)
                        "Code" -> onAddCustomCode(param1)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF04101A))
            ) {
                Text("Insert Node")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CyberTextSecondary)
            }
        },
        containerColor = CyberCard,
        shape = RoundedCornerShape(16.dp)
    )
}
