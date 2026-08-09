package com.playermp3.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.playermp3.data.AudioTrack
import com.playermp3.playback.PlayerUiState
import com.playermp3.ui.AlbumArt
import com.playermp3.ui.GlassSurface
import com.playermp3.ui.RoundControl
import com.playermp3.ui.SectionHeader
import com.playermp3.ui.theme.TextPrimary
import com.playermp3.ui.theme.TextSecondary
import com.playermp3.ui.theme.TextTertiary

@Composable
fun LibraryScreen(
    ui: PlayerUiState,
    onPlay: (AudioTrack, List<AudioTrack>) -> Unit,
    onRefresh: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            Spacer(Modifier.height(8.dp))
        }

        when {
            ui.loading && ui.library == null -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = TextSecondary)
                }
            }

            !ui.hasPermission -> item {
                LibraryNotice(
                    title = "Audio access needed",
                    body = "Allow music access to import the songs stored on this device.",
                )
            }

            ui.library == null -> item {
                LibraryNotice(
                    title = "Audio access needed",
                    body = "Allow music access to import the songs stored on this device.",
                )
            }

            ui.library?.allSongs?.isEmpty() == true -> item {
                LibraryNotice(
                    title = "No music found",
                    body = "Add MP3 files to your device or pick a music folder in Settings.",
                    onRefresh = onRefresh,
                )
            }

            else -> {
                val library = ui.library!!
                if (ui.recentlyPlayed.isNotEmpty()) {
                    item {
                        SectionHeader(
                            text = "Recently Played",
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp),
                        )
                    }
                    items(
                        items = ui.recentlyPlayed,
                        key = { "recent-${it.id}" },
                    ) { track ->
                        PlayedRow(
                            track = track,
                            onClick = { onPlay(track, library.allSongs) },
                        )
                    }
                }

                item {
                    SectionHeader(
                        text = "All Songs",
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp),
                    )
                }

                items(
                    items = library.allSongs,
                    key = { it.id },
                ) { track ->
                    PlayedRow(
                        track = track,
                        onClick = { onPlay(track, library.allSongs) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayedRow(
    track: AudioTrack,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArt(
            artworkUri = track.artworkUri?.toString(),
            albumTitle = track.album,
            size = 48.dp,
            corner = 12.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
            )
        }
        Text(
            text = formatDuration(track.durationMs),
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L)) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun LibraryNotice(
    title: String,
    body: String,
    onRefresh: (() -> Unit)? = null,
) {
    GlassSurface(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            if (onRefresh != null) {
                Spacer(Modifier.height(16.dp))
                RoundControl(
                    icon = Icons.Filled.Refresh,
                    contentDescription = "Rescan",
                    background = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f),
                    onClick = onRefresh,
                )
            }
        }
    }
}