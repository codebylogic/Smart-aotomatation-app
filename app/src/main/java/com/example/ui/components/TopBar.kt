package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun NexusTopBar(
    title: String,
    onCpuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("nexus_top_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hexagon / Cyber node logo badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberCard)
                    .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Futuristic network node glyph
                Text(
                    text = "⎈",
                    color = CyberCyan,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = title,
                color = CyberTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onCpuClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CyberCard)
                    .border(1.dp, CyberBorder, CircleShape)
                    .testTag("top_bar_cpu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = "System Daemon Status",
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CyberCard)
                    .border(1.dp, CyberBorder, CircleShape)
                    .testTag("top_bar_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile and Workspaces",
                    tint = CyberTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
