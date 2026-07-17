package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF381E72),            // Deep Violet/Purple on Primary Lavender
    secondary = CyberBlue,
    onSecondary = Color(0xFF1D192B),          // Dark Purple on Pale Lavender
    tertiary = AccentEmerald,
    background = DeepObsidian,
    onBackground = TextWhite,
    surface = DarkNavy,
    onSurface = TextWhite,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextMuted,
    error = DangerCrimson,
    onError = Color(0xFF601410)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
