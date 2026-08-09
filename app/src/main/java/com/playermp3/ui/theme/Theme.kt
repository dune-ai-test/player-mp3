package com.playermp3.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val storage: String) {
    Dark("dark"),
    Light("light"),
    System("system");

    companion object {
        fun from(value: String): ThemeMode =
            entries.firstOrNull { it.storage == value } ?: Dark
    }
}

@Composable
fun CadenceTheme(
    themeMode: ThemeMode = ThemeMode.Dark,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
        ThemeMode.System -> isSystemInDarkTheme()
    }
    val design = if (dark) CadenceDark else CadenceLight
    val scheme = if (dark) {
        darkColorScheme(
            primary = design.accent,
            onPrimary = design.controlIcon,
            secondary = design.textSecondary,
            onSecondary = design.text,
            background = design.background.first(),
            onBackground = design.text,
            surface = design.surfaceStrong,
            onSurface = design.text,
            surfaceVariant = design.surface,
            onSurfaceVariant = design.textSecondary,
            outline = design.border,
            outlineVariant = design.border,
            error = Color(0xFFF87171),
        )
    } else {
        lightColorScheme(
            primary = design.accent,
            onPrimary = Color.White,
            secondary = design.textSecondary,
            onSecondary = design.text,
            background = design.background.first(),
            onBackground = design.text,
            surface = design.surfaceStrong,
            onSurface = design.text,
            surfaceVariant = design.surface,
            onSurfaceVariant = design.textSecondary,
            outline = design.border,
            outlineVariant = design.border,
            error = Color(0xFFB3261E),
        )
    }

    CompositionLocalProvider(LocalAppDesign provides design) {
        MaterialTheme(
            colorScheme = scheme,
            typography = CadenceTypography,
            content = content,
        )
    }
}