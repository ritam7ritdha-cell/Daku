package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DakuColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00363D),
    onPrimaryContainer = CyberCyan,
    secondary = NeonViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF380E5E),
    onSecondaryContainer = Color(0xFFE8C5FF),
    tertiary = GlowingAmber,
    onTertiary = Color.Black,
    background = DeepMidnight,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
)

@Composable
fun DakuAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DakuColorScheme,
        typography = Typography,
        content = content
    )
}
