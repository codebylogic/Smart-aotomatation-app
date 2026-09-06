package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF04101A),
    primaryContainer = Color(0xFF0F3248),
    onPrimaryContainer = CyberCyan,
    secondary = CyberBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF162A4E),
    onSecondaryContainer = Color(0xFF93C5FD),
    tertiary = CyberGreen,
    onTertiary = Color(0xFF022013),
    background = CyberBackground,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberCard,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberBorder,
    outlineVariant = CyberBorderSubtle,
    error = CyberRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}

