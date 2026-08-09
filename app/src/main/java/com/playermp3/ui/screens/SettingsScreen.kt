package com.playermp3.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.playermp3.data.AppSettings
import com.playermp3.playback.PlayerUiState
import com.playermp3.ui.GlassSurface
import com.playermp3.ui.HomeTab
import com.playermp3.ui.TopNav
import com.playermp3.ui.theme.LocalAppDesign
import com.playermp3.ui.theme.MidnightGradient
import com.playermp3.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    ui: PlayerUiState,
    settings: AppSettings,
    selectedTab: HomeTab,
    onTabSelect: (HomeTab) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onPickFolders: () -> Unit,
    onClearFolders: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleEqualizer: () -> Unit,
    onToggleGapless: () -> Unit,
    onOpenNowPlaying: () -> Unit,
) {
    val context = LocalContext.current
    val trackCount = ui.library?.allSongs?.size ?: 0
    val folderCount = settings.folders.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 4.dp,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TopNav(selected = selectedTab, onSelect = onTabSelect)
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(LocalAppDesign.current.surface)
                    .clickable { onPickFolders() }
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RowIcon(Icons.Filled.LibraryMusic, LocalAppDesign.current.surfaceStrong)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Local Music Library",
                            style = MaterialTheme.typography.titleSmall,
                            color = LocalAppDesign.current.text,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = if (folderCount > 0) "$trackCount tracks · $folderCount folders" else "$trackCount tracks · whole device",
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalAppDesign.current.textSecondary,
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = LocalAppDesign.current.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        if (folderCount > 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "Scan only these folders · Clear",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalAppDesign.current.accent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onClearFolders() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalAppDesign.current.text,
                )
                ThemeSelect(
                    current = settings.themeMode,
                    onSelect = onThemeMode,
                )
            }
        }

        item {
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.titleMedium,
                color = LocalAppDesign.current.text,
            )
        }

        item {
            SettingsPanel {
                SettingsRow(
                    icon = Icons.Filled.PlayArrow,
                    iconBg = LocalAppDesign.current.surfaceStrong,
                    label = "Now Playing",
                    onClick = onOpenNowPlaying,
                )
                SettingsRow(
                    icon = Icons.Filled.Speed,
                    iconBg = LocalAppDesign.current.surfaceStrong,
                    label = "Playback Speed",
                    value = speedLabel(settings.playbackSpeed),
                    onClick = onCycleSpeed,
                )
                SettingsRow(
                    icon = Icons.Filled.VolumeUp,
                    iconBg = LocalAppDesign.current.surfaceStrong,
                    label = "Audio Output",
                    value = "Device",
                    onClick = {
                        Toast.makeText(context, "Audio plays through the device speaker.", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }

        item {
            SettingsPanel {
                SettingsRow(
                    icon = Icons.Filled.Storage,
                    iconBg = LocalAppDesign.current.surfaceStrong,
                    label = "Storage",
                    value = "$trackCount tracks",
                    onClick = {
                        Toast.makeText(context, "Scanning mode: ${if (folderCount > 0) "$folderCount folder(s)" else "whole device"}.", Toast.LENGTH_SHORT).show()
                    },
                )
                SettingsRow(
                    icon = Icons.Filled.Info,
                    iconBg = LocalAppDesign.current.surfaceStrong,
                    label = "About",
                    onClick = {
                        Toast.makeText(context, "Cadence v1.0 — local MP3 player", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }

        item {
            Text(
                text = "Playback",
                style = MaterialTheme.typography.titleMedium,
                color = LocalAppDesign.current.text,
            )
        }

        item {
            SettingsPanel {
                SettingsRow(
                    icon = Icons.Filled.Equalizer,
                    iconBg = LocalAppDesign.current.surfaceStrong,
                    label = "Equalizer",
                    toggle = { PillToggle(checked = settings.equalizer, onChange = onToggleEqualizer) },
                )
                SettingsRow(
                    icon = Icons.Filled.Bolt,
                    iconBg = LocalAppDesign.current.surfaceStrong,
                    label = "Gapless Playback",
                    toggle = { PillToggle(checked = settings.gapless, onChange = onToggleGapless) },
                )
                SettingsRow(
                    icon = Icons.Filled.Repeat,
                    iconBg = LocalAppDesign.current.surfaceStrong,
                    label = "Repeat All",
                    toggle = { PillToggle(checked = ui.repeatAll, onChange = onToggleRepeat) },
                )
                SettingsRow(
                    icon = Icons.Filled.Shuffle,
                    iconBg = LocalAppDesign.current.surfaceStrong,
                    label = "Shuffle All",
                    toggle = { PillToggle(checked = ui.shuffleOn, onChange = onToggleShuffle) },
                )
            }
        }
    }
}

@Composable
private fun RowIcon(icon: ImageVector, background: Color) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LocalAppDesign.current.text,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun SettingsPanel(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(LocalAppDesign.current.surface),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconBg: Color,
    label: String,
    value: String? = null,
    onClick: () -> Unit = {},
    toggle: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = toggle == null) { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LocalAppDesign.current.text,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalAppDesign.current.text,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = LocalAppDesign.current.textSecondary,
            )
            Spacer(Modifier.width(6.dp))
        }
        if (toggle != null) {
            toggle()
        } else if (value == null) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = LocalAppDesign.current.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun PillToggle(
    checked: Boolean,
    onChange: () -> Unit,
) {
    val d = LocalAppDesign.current
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (checked) d.toggleOn else d.toggleOff)
            .clickable { onChange() }
            .padding(horizontal = 3.dp, vertical = 3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (checked) d.toggleOnKnob else d.toggleOffKnob)
        )
    }
}

@Composable
private fun ThemeSelect(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val d = LocalAppDesign.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(d.surface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ThemeOption(
            label = "Midnight",
            swatch = { Box(Modifier.size(36.dp).clip(CircleShape).background(Brush.linearGradient(MidnightGradient))) },
            active = current == ThemeMode.Dark,
            onClick = { onSelect(ThemeMode.Dark) },
        )
        ThemeOption(
            label = "Light",
            swatch = { Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White).then(Modifier)) },
            active = current == ThemeMode.Light,
            onClick = { onSelect(ThemeMode.Light) },
        )
        ThemeOption(
            label = "Auto",
            swatch = { Box(Modifier.size(36.dp).clip(CircleShape).background(Brush.linearGradient(d.background))) },
            active = current == ThemeMode.System,
            onClick = { onSelect(ThemeMode.System) },
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ThemeOption(
    label: String,
    swatch: @Composable () -> Unit,
    active: Boolean,
    onClick: () -> Unit,
) {
    val d = LocalAppDesign.current
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) d.navActiveBg else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        swatch()
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (active) androidx.compose.ui.text.font.FontWeight.SemiBold
                else androidx.compose.ui.text.font.FontWeight.Medium
            ),
            color = if (active) d.navActiveText else d.navInactiveText,
        )
    }
}

private fun speedLabel(speed: Float): String {
    val text = if (speed == 1.0f) "Normal" else "${speed}x"
    return text
}