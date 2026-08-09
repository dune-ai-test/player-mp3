package com.playermp3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.playermp3.playback.PlayerUiState
import com.playermp3.ui.AlbumArt
import com.playermp3.ui.GlassSurface
import com.playermp3.ui.RoundControl
import com.playermp3.ui.StreamBigButton
import com.playermp3.ui.formatTime
import com.playermp3.ui.theme.Charcoal
import com.playermp3.ui.theme.IconGray
import com.playermp3.ui.theme.TextPrimary
import com.playermp3.ui.theme.TextSecondary
import com.playermp3.ui.theme.TrackFill

@Composable
fun NowPlayingScreen(
    ui: PlayerUiState,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
) {
    val track = ui.currentTrack
    if (track == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Nothing playing",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        return
    }

    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val scrollState = rememberScrollState()
    val dismissThreshold = with(LocalDensity.current) { 150.dp.toPx() }
    val duration = ui.durationMs.coerceAtLeast(track.durationMs)
    val trackDuration = duration.coerceAtLeast(1L)
    val fraction = if (duration > 0L) {
        (ui.positionMs.toFloat() / trackDuration.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val displayFraction = (dragFraction ?: fraction).coerceIn(0f, 1f)
    val displayPosition = dragFraction?.let { (it * trackDuration).toLong() } ?: ui.positionMs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .pointerInput(Unit) {
                // Swipe down to collapse, like tapping the back button.
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var total = 0f
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                            ?: return@awaitEachGesture
                        if (!change.pressed) break
                        val delta = change.positionChange().y
                        if (delta > 0f) total += delta
                        if (total > dismissThreshold && scrollState.value == 0) {
                            onBack()
                            break
                        }
                    }
                }
            }
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundControl(
                icon = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Collapse",
                color = TextPrimary,
                size = 44.dp,
                padding = 26.dp,
                onClick = onBack,
            )
            Spacer(Modifier.weight(1f))
            RoundControl(
                icon = Icons.Filled.MoreVert,
                contentDescription = "More options",
                color = TextSecondary,
                size = 44.dp,
                padding = 26.dp,
                onClick = {},
            )
        }

        Spacer(Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            AlbumArt(
                artworkUri = track.artworkUri?.toString(),
                albumTitle = track.album,
                corner = 36.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
            )
        }

        Spacer(Modifier.height(22.dp))

        Text(
            text = "NOW PLAYING",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = track.title,
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = track.artist,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(26.dp))

        GlassSurface(corner = 28.dp, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RoundControl(
                        icon = Icons.Filled.QueueMusic,
                        contentDescription = "Queue",
                        color = TextSecondary,
                        onClick = {},
                    )
                    RoundControl(
                        icon = Icons.Filled.Airplay,
                        contentDescription = "Play on device",
                        color = TextSecondary,
                        onClick = {},
                    )
                }

                Spacer(Modifier.height(10.dp))

                Slider(
                    value = displayFraction,
                    onValueChange = { dragFraction = it.coerceIn(0f, 1f) },
                    onValueChangeFinished = {
                        dragFraction?.let { onSeekTo((it * trackDuration).toLong()) }
                        dragFraction = null
                    },
                    enabled = duration > 0L,
                    colors = SliderDefaults.colors(
                        thumbColor = TextPrimary,
                        activeTrackColor = TextPrimary,
                        inactiveTrackColor = TrackFill,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatTime(displayPosition),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.72f),
                        color = TextSecondary,
                    )
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.72f),
                        color = TextSecondary,
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RoundControl(
                        icon = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        size = 46.dp,
                        padding = 24.dp,
                        color = if (ui.shuffleOn) TextPrimary else IconGray,
                        onClick = onToggleShuffle,
                    )
                    RoundControl(
                        icon = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        size = 52.dp,
                        padding = 26.dp,
                        color = TextPrimary,
                        onClick = onPrevious,
                    )
                    StreamBigButton(
                        isPlaying = ui.isPlaying,
                        size = 66.dp,
                        onClick = onTogglePlay,
                    )
                    RoundControl(
                        icon = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        size = 52.dp,
                        padding = 26.dp,
                        color = TextPrimary,
                        onClick = onNext,
                    )
                    RoundControl(
                        icon = Icons.Filled.Repeat,
                        contentDescription = "Repeat",
                        size = 46.dp,
                        padding = 24.dp,
                        color = if (ui.repeatAll) TextPrimary else IconGray,
                        onClick = onToggleRepeat,
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}