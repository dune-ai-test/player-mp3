package com.playermp3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.playermp3.playback.PlayerUiState
import com.playermp3.ui.GlassSurface
import com.playermp3.ui.SectionHeader
import com.playermp3.ui.theme.TextPrimary
import com.playermp3.ui.theme.TextSecondary
import com.playermp3.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    ui: PlayerUiState,
    onRefresh: () -> Unit,
) {
    val trackCount = ui.library?.allSongs?.size ?: 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 8.dp,
            bottom = 20.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionHeader(text = "Settings")
        }
        item {
            GlassSurface(corner = 20.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AudioFile,
                            contentDescription = null,
                            tint = TextSecondary,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Library",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "$trackCount songs",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (ui.hasPermission)
                            "Music access granted — all MP3 files on this device are scanned automatically."
                        else
                            "Music access not granted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }
        item {
            GlassSurface(corner = 20.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRefresh() }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = TextSecondary,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Rescan library",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                }
            }
        }
        item {
            GlassSurface(corner = 20.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = TextSecondary,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Cadence — a local MP3 player built with Jetpack Compose and Media3 (ExoPlayer). Playback keeps running in the background with a media notification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}