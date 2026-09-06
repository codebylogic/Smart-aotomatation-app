package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.permissions.PermissionItem
import com.example.ui.MainViewModel
import com.example.ui.components.NexusTopBar
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun PermissionsScreen(
    viewModel: MainViewModel,
    onProceedToCanvas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val permState by viewModel.permissionsState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("permissions_screen")
    ) {
        NexusTopBar(
            title = "Permissions",
            onCpuClick = { viewModel.restartEngineDaemon() },
            onProfileClick = {}
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: System Calibration
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "System Calibration",
                    color = CyberTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dual-engine automation framework requires lower-level Android primitives to orchestrate device workflows.",
                    color = CyberTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            // Hardware Readiness Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HARDWARE READINESS",
                                color = CyberTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${permState.grantedCount}/${permState.totalCount}",
                                    color = CyberTextPrimary,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${permState.percentage}% Calibrated",
                                    color = CyberCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        // Circular Progress Dial
                        Box(
                            modifier = Modifier.size(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color(0xFF1E293B),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = CyberCyan,
                                    startAngle = -90f,
                                    sweepAngle = (permState.percentage * 3.6f),
                                    useCenter = false,
                                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Segmented Step Bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0 until permState.totalCount) {
                            val isFilled = i < permState.grantedCount
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isFilled) CyberCyan else Color(0xFF1A263B))
                            )
                        }
                    }

                    // Banner tip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0A1322))
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Grant remaining permissions for background automation and hardware hooks.",
                            color = CyberTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Section 1: SPECIAL ACCESS HOOKS
            SectionHeader(title = "SPECIAL ACCESS HOOKS", badge = "Core Engine")
            permState.items.filter { it.isSpecialAccess }.forEach { item ->
                PermissionCard(
                    item = item,
                    onAction = { viewModel.grantPermission(item) }
                )
            }

            // Section 2: RUNTIME HARDWARE SENSORS
            SectionHeader(title = "RUNTIME HARDWARE SENSORS", badge = "Context Hooks")
            permState.items.filter { !it.isSpecialAccess }.forEach { item ->
                PermissionCard(
                    item = item,
                    onAction = { viewModel.grantPermission(item) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            OutlinedButton(
                onClick = { viewModel.refreshPermissions() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("scan_recheck_privileges_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberTextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan & Re-check Privileges",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onProceedToCanvas,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("proceed_to_canvas_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = Color(0xFF04101A)
                )
            ) {
                Text(
                    text = "Proceed to Visual Canvas",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, badge: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = CyberTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = badge,
            color = CyberTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun PermissionCard(
    item: PermissionItem,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("perm_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCardElevated)
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (item.id) {
                            "accessibility" -> Icons.Default.AccessibilityNew
                            "overlay" -> Icons.Default.Layers
                            "write_settings" -> Icons.Default.Settings
                            "battery" -> Icons.Default.BatteryChargingFull
                            "geofence" -> Icons.Default.LocationOn
                            "camera" -> Icons.Default.FlashlightOn
                            else -> Icons.Default.Notifications
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (item.isGranted) CyberCyan else CyberTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.title,
                                color = CyberTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        // Badge status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (item.isGranted) CyberCyan.copy(alpha = 0.15f)
                                    else CyberPurple.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (item.isGranted) "✓ Granted" else "Action Needed",
                                color = if (item.isGranted) CyberCyan else CyberPurple,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // If granted or toggleable
                if (item.isGranted || item.id == "battery" || item.id == "accessibility" || item.id == "camera") {
                    Switch(
                        checked = item.isGranted,
                        onCheckedChange = { onAction() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF04101A),
                            checkedTrackColor = CyberCyan,
                            uncheckedTrackColor = CyberCardElevated
                        )
                    )
                }
            }

            Text(
                text = item.description,
                color = CyberTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            // Direct Grant button for special permissions
            if (!item.isGranted && (item.id == "write_settings" || item.id == "geofence" || item.id == "notification")) {
                Button(
                    onClick = onAction,
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("grant_button_${item.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16223B),
                        contentColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.actionLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
