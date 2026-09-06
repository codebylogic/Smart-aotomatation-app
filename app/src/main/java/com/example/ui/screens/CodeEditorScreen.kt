package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExecutionLog
import com.example.data.model.LogLevel
import com.example.ui.MainViewModel
import com.example.ui.components.NexusTopBar
import com.example.ui.theme.CodeComment
import com.example.ui.theme.CodeFunction
import com.example.ui.theme.CodeKeyword
import com.example.ui.theme.CodeNumber
import com.example.ui.theme.CodeObject
import com.example.ui.theme.CodeString
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun CodeEditorScreen(
    viewModel: MainViewModel,
    onNavigateToCanvas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rawCode by viewModel.codeState.collectAsState()
    val logs by viewModel.logsState.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()

    var activeTab by remember { mutableStateOf("main.js*") }
    var isConsoleExpanded by remember { mutableStateOf(true) }

    var editorTextFieldValue by remember(rawCode) {
        mutableStateOf(TextFieldValue(rawCode))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("code_editor_screen")
    ) {
        NexusTopBar(
            title = "Code Editor",
            onCpuClick = { viewModel.restartEngineDaemon() },
            onProfileClick = {}
        )

        // Subheader with engine badge, node link status, sync and run
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Night Silent & Torc... •",
                    color = CyberTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0D253A))
                            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡ V8 / QUICKJS MICRO",
                            color = CyberCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "• Node Link Active",
                        color = CyberTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View Canvas / Sync button
                IconButton(
                    onClick = {
                        viewModel.syncCodeToVisual()
                        onNavigateToCanvas()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCard)
                        .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                        .testTag("editor_view_canvas_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SyncAlt,
                        contentDescription = "Sync & View Canvas",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.syncCodeToVisual() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCard)
                        .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Code",
                        tint = CyberTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = { viewModel.runCurrentScript("Code Editor Run Button") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color(0xFF04101A)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("editor_run_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isExecuting) "RUNNING" else "RUN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // File Tabs Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EditorTabItem(
                title = "main.js*",
                icon = {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(CyberCyan))
                },
                isSelected = activeTab == "main.js*",
                hasClose = true,
                onClick = { activeTab = "main.js*" }
            )
            EditorTabItem(
                title = "globals.d.ts",
                icon = {
                    Icon(Icons.Default.Description, contentDescription = null, tint = CyberTextMuted, modifier = Modifier.size(14.dp))
                },
                isSelected = activeTab == "globals.d.ts",
                hasClose = false,
                onClick = { activeTab = "globals.d.ts" }
            )
            EditorTabItem(
                title = "config.json",
                icon = {
                    Icon(Icons.Default.DataObject, contentDescription = null, tint = CyberTextMuted, modifier = Modifier.size(14.dp))
                },
                isSelected = activeTab == "config.json",
                hasClose = false,
                onClick = { activeTab = "config.json" }
            )
        }

        // Code Editor Canvas area
        Box(
            modifier = Modifier
                .weight(if (isConsoleExpanded) 1f else 2f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF070B13))
                .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
        ) {
            // RAM badge in top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberCardElevated.copy(alpha = 0.85f))
                    .border(1.dp, CyberBorderSubtle, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                    Text("RAM: 4.8MB", color = CyberCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }

            // Line numbers + code text field
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp)
            ) {
                // Line numbers
                val lineCount = editorTextFieldValue.text.lines().size.coerceAtLeast(20)
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lineCount) {
                        Text(
                            text = String.format("%02d", i),
                            color = Color(0xFF3B4861),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Code text field with real-time editing & syntax highlighting
                BasicTextField(
                    value = editorTextFieldValue,
                    onValueChange = {
                        editorTextFieldValue = it
                        viewModel.updateCode(it.text)
                    },
                    textStyle = TextStyle(
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp
                    ),
                    visualTransformation = { text ->
                        androidx.compose.ui.text.input.TransformedText(
                            highlightSyntax(text.text),
                            androidx.compose.ui.text.input.OffsetMapping.Identity
                        )
                    },
                    cursorBrush = SolidColor(CyberCyan),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .testTag("code_editor_text_field")
                )
            }
        }

        // Quick Snippet Keyboard Keys Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF090E18))
                .border(1.dp, CyberBorderSubtle)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "KEYS",
                color = CyberTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            val keys = listOf("()", "{}", "=>", "await", "Device.", "WiFi.", ";", "$")
            keys.forEach { key ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberCardElevated)
                        .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            val cur = editorTextFieldValue.text
                            val newText = cur + key
                            editorTextFieldValue = TextFieldValue(newText)
                            viewModel.updateCode(newText)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("key_snippet_$key")
                ) {
                    Text(
                        text = key,
                        color = when (key) {
                            "Device." -> CyberCyan
                            "WiFi." -> CyberBlue
                            "await" -> CodeKeyword
                            else -> CyberTextPrimary
                        },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Execution Console & Logs (Collapsible bottom panel)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(CyberCard)
                .border(1.dp, CyberBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            // Console Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isConsoleExpanded = !isConsoleExpanded }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Execution Console & Logs",
                        color = CyberTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Live",
                            color = CyberCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.clearLogs() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear logs",
                            tint = CyberTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isConsoleExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = CyberTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isConsoleExpanded) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .background(Color(0xFF060910))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(logs) { log ->
                        ConsoleLogRow(log)
                    }
                }
            }
        }
    }
}

@Composable
fun ConsoleLogRow(log: ExecutionLog) {
    val levelColor = when (log.level) {
        LogLevel.INFO -> CyberBlue
        LogLevel.DEBUG -> CyberPurple
        LogLevel.SUCCESS -> CyberGreen
        LogLevel.WARN -> CyberAmber
        LogLevel.ERROR -> CyberRed
        LogLevel.STATUS -> CyberPurple
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = log.timestamp,
            color = CyberTextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(levelColor.copy(alpha = 0.15f))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = log.level.name,
                color = levelColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = log.message,
            color = CyberTextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EditorTabItem(
    title: String,
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    hasClose: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberCardElevated else Color.Transparent)
            .border(1.dp, if (isSelected) CyberBorder else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon()
        Text(
            text = title,
            color = if (isSelected) CyberTextPrimary else CyberTextMuted,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (hasClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = CyberTextMuted,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

fun highlightSyntax(code: String): AnnotatedString {
    return buildAnnotatedString {
        append(code)

        val keywords = listOf(
            "import", "from", "export", "default", "async", "function",
            "const", "let", "var", "await", "if", "else", "return", "true", "false", "new"
        )
        val apis = listOf("Device", "WiFi", "System", "Logger")
        val methods = listOf(
            "getConnectedSSID", "setRingerMode", "setVolume", "toggleTorch",
            "vibrate", "info", "warn", "error", "debug", "run", "now"
        )

        // Keywords
        keywords.forEach { kw ->
            val regex = Regex("\\b$kw\\b")
            regex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = CodeKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            }
        }

        // APIs
        apis.forEach { api ->
            val regex = Regex("\\b$api\\b")
            regex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = CodeObject, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
            }
        }

        // Methods
        methods.forEach { m ->
            val regex = Regex("\\b$m\\b")
            regex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = CodeFunction), match.range.first, match.range.last + 1)
            }
        }

        // Strings '...' or "..."
        val stringRegex = Regex("['\"][^'\"]*['\"]")
        stringRegex.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = CodeString), match.range.first, match.range.last + 1)
        }

        // Comments // ...
        val commentRegex = Regex("//.*")
        commentRegex.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = CodeComment), match.range.first, match.range.last + 1)
        }

        // Numbers
        val numRegex = Regex("\\b\\d+\\b")
        numRegex.findAll(code).forEach { match ->
            addStyle(SpanStyle(color = CodeNumber), match.range.first, match.range.last + 1)
        }
    }
}
