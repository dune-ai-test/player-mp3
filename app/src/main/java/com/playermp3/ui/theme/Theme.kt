package com.playermp3.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CadenceColorScheme = darkColorScheme(
    primary = TextPrimary,
    onPrimary = Charcoal,
    primaryContainer = TextPrimary,
    onPrimaryContainer = Charcoal,
    secondary = IconGray,
    onSecondary = Night,
    secondaryContainer = GlassCard,
    onSecondaryContainer = TextSecondary,
    background = Night,
    onBackground = TextPrimary,
    surface = Card,
    onSurface = TextPrimary,
    surfaceVariant = GlassCard,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassBorder,
    error = Color(0xFFF87171),
    onError = Color(0xFF111111),
)

@Composable
fun CadenceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CadenceColorScheme,
        typography = CadenceTypography,
        content = content,
    )
}