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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.playermp3.data.Album
import com.playermp3.data.AudioTrack
import com.playermp3.playback.PlayerUiState
import com.playermp3.ui.AlbumArt
import com.playermp3.ui.GlassSurface
import com.playermp3.ui.RoundControl
import com.playermp3.ui.SectionHeader
import com.playermp3.ui.TrackRow
import com.playermp3.ui.theme.TextPrimary
import com.playermp3.ui.theme.TextSecondary
import com.playermp3.ui.theme.TextTertiary

@Composable
fun LibraryScreen(
    ui: PlayerUiState,
    onPlay: (AudioTrack, List<AudioTrack>) -> Unit,
    onAlbum: (Album) -> Unit,
    onOpenSearch: () -> Unit,
    onRefresh: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.weight(1f))
                RoundControl(
                    icon = Icons.Filled.Search,
                    contentDescription = "Search",
                    color = TextSecondary,
                    onClick = onOpenSearch,
                )
            }
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
                PermissionPrompt(onRefresh = onRefresh)
            }

            ui.library == null -> item {
                PermissionPrompt(onRefresh = onRefresh)
            }

            ui.library?.allSongs?.isEmpty() == true -> item {
                NoMusicPrompt(onRefresh = onRefresh)
            }

            else -> {
                val library = ui.library!!
                if (ui.recentlyPlayed.isNotEmpty()) {
                    item {
                        Column(Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                            SectionHeader(
                                text = "Recently Played",
                                modifier = Modifier.padding(horizontal = 20.dp),
                            )
                        }
                    }
                    item {
                        LazyRow(
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 20.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(ui.recentlyPlayed, key = { it.id }) { track ->
                                TrackCard(
                                    track = track,
                                    onClick = { onPlay(track, library.allSongs) },
                                )
                            }
                        }
                    }
                }

                item {
                    Column(Modifier.padding(top = 20.dp, bottom = 6.dp)) {
                        SectionHeader(
                            text = "All Songs",
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }

                items(
                    items = library.allSongs,
                    key = { it.id },
                ) { track ->
                    TrackRow(
                        track = track,
                        onClick = { onPlay(track, library.allSongs) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackCard(
    track: AudioTrack,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(148.dp)
            .clickable { onClick() }
    ) {
        AlbumArt(
            artworkUri = track.artworkUri?.toString(),
            albumTitle = track.album,
            size = 148.dp,
            corner = 18.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
        )
        Text(
            text = track.artist,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1,
        )
    }
}

@Composable
private fun NoMusicPrompt(onRefresh: () -> Unit) {
    GlassSurface(modifier = Modifier.padding(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No music found",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Add MP3 files to your device, then rescan the library.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
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

@Composable
private fun PermissionPrompt(onRefresh: () -> Unit) {
    GlassSurface(modifier = Modifier.padding(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Audio access needed",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Allow music access to import the songs stored on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}