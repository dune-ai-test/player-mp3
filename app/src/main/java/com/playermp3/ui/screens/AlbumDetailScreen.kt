package com.playermp3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.playermp3.data.Album
import com.playermp3.data.AudioTrack
import com.playermp3.playback.PlayerUiState
import com.playermp3.ui.AlbumArt
import com.playermp3.ui.HomeTab
import com.playermp3.ui.RoundControl
import com.playermp3.ui.TopNav
import com.playermp3.ui.TrackRow
import com.playermp3.ui.formatTime
import com.playermp3.ui.theme.GlassCard
import com.playermp3.ui.theme.PaperText
import com.playermp3.ui.theme.TextPrimary
import com.playermp3.ui.theme.TextSecondary
import com.playermp3.ui.theme.TextTertiary

@Composable
fun AlbumDetailScreen(
    album: Album,
    ui: PlayerUiState,
    onTabSelect: (HomeTab) -> Unit,
    onPlay: (AudioTrack, List<AudioTrack>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        TopNav(
            selected = HomeTab.Albums,
            onSelect = onTabSelect,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AlbumArt(
                        artworkUri = album.artworkUri?.toString(),
                        albumTitle = album.name,
                        corner = 32.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .aspectRatio(1f),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = album.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayAllButton(
                        onClick = {
                            album.tracks.firstOrNull()?.let { onPlay(it, album.tracks) }
                        }
                    )
                    RoundControl(
                        icon = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle album",
                        size = 46.dp,
                        padding = 24.dp,
                        color = TextPrimary,
                        background = GlassCard,
                        onClick = {
                            val shuffled = album.tracks.shuffled()
                            shuffled.firstOrNull()?.let { onPlay(it, shuffled) }
                        },
                    )
                    RoundControl(
                        icon = Icons.Filled.Download,
                        contentDescription = "Download",
                        size = 46.dp,
                        padding = 24.dp,
                        color = TextSecondary,
                        background = GlassCard,
                        onClick = {},
                    )
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
            }

            itemsIndexed(
                items = album.tracks,
                key = { _, track -> track.id },
            ) { index, track ->
                TrackRow(
                    track = track,
                    number = index + 1,
                    onClick = { onPlay(track, album.tracks) },
                )
            }

            item {
                Text(
                    text = "${album.tracks.size} songs · ${formatTime(album.tracks.sumOf { it.durationMs })} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PlayAllButton(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(listOf(TextPrimary, TextPrimary.copy(alpha = 0.9f))))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = PaperText,
        )
        Text(
            text = "Play All",
            style = MaterialTheme.typography.titleSmall,
            color = PaperText,
        )
    }
}