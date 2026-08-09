package com.playermp3.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppDesign(
    val background: List<Color>,
    val surface: Color,
    val surfaceStrong: Color,
    val border: Color,
    val text: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val controlBg: Color,
    val controlIcon: Color,
    val navBg: Color,
    val navActiveBg: Color,
    val navActiveText: Color,
    val navInactiveText: Color,
    val toggleOn: Color,
    val toggleOnKnob: Color,
    val toggleOff: Color,
    val toggleOffKnob: Color,
    val trackFill: Color,
)

val MidnightGradient = listOf(
    Color(0xFF7B7BE0),
    Color(0xFF4646A8),
    Color(0xFF23235E),
)

val CadenceDark = AppDesign(
    background = listOf(Color(0xFF12122E), Color(0xFF0B0B1C)),
    surface = Color(0x14FFFFFF),
    surfaceStrong = Color(0x2EFFFFFF),
    border = Color(0x21FFFFFF),
    text = Color(0xFFF6F6FF),
    textSecondary = Color(0xB3FFFFFF),
    textTertiary = Color(0x80FFFFFF),
    accent = Color(0xFF7C7BE0),
    controlBg = Color(0xFFFFFFFF),
    controlIcon = Color(0xFF23235E),
    navBg = Color(0x14FFFFFF),
    navActiveBg = Color(0xFFFFFFFF),
    navActiveText = Color(0xFF23235E),
    navInactiveText = Color(0xB3FFFFFF),
    toggleOn = Color(0xFFFFFFFF),
    toggleOnKnob = Color(0xFF23235E),
    toggleOff = Color(0x2EFFFFFF),
    toggleOffKnob = Color(0xFFFFFFFF),
    trackFill = Color(0x2EFFFFFF),
)

val CadenceLight = AppDesign(
    background = listOf(Color(0xFFECEEFB), Color(0xFFD8DCF1)),
    surface = Color(0x1F23235E),
    surfaceStrong = Color(0x3323235E),
    border = Color(0x2623235E),
    text = Color(0xFF23235E),
    textSecondary = Color(0xB323235E),
    textTertiary = Color(0x6623235E),
    accent = Color(0xFF5B5BC8),
    controlBg = Color(0xFF23235E),
    controlIcon = Color(0xFFFFFFFF),
    navBg = Color(0x1F23235E),
    navActiveBg = Color(0xFF23235E),
    navActiveText = Color(0xFFFFFFFF),
    navInactiveText = Color(0x9923235E),
    toggleOn = Color(0xFF23235E),
    toggleOnKnob = Color(0xFFFFFFFF),
    toggleOff = Color(0x1F23235E),
    toggleOffKnob = Color(0xFF23235E),
    trackFill = Color(0x4023235E),
)

val LocalAppDesign = staticCompositionLocalOf { CadenceDark }

// Themed accessors — existing code keeps compiling, colors follow the active theme.
val Night: Color
    @Composable get() = LocalAppDesign.current.background.first()

val Card: Color
    @Composable get() = LocalAppDesign.current.surfaceStrong

val Charcoal: Color
    @Composable get() = LocalAppDesign.current.controlBg

val DeepNavy: Color
    @Composable get() = LocalAppDesign.current.surfaceStrong

val IconGray: Color
    @Composable get() = LocalAppDesign.current.textSecondary

val GlassCard: Color
    @Composable get() = LocalAppDesign.current.surface

val GlassBorder: Color
    @Composable get() = LocalAppDesign.current.border

val PaperText: Color
    @Composable get() = LocalAppDesign.current.controlIcon

val TextPrimary: Color
    @Composable get() = LocalAppDesign.current.text

val TextSecondary: Color
    @Composable get() = LocalAppDesign.current.textSecondary

val TextTertiary: Color
    @Composable get() = LocalAppDesign.current.textTertiary

val TrackFill: Color
    @Composable get() = LocalAppDesign.current.trackFill