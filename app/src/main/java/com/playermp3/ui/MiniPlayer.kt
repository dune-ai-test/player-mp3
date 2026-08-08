package com.playermp3.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.playermp3.data.AudioTrack
import com.playermp3.ui.theme.DeepNavy
import com.playermp3.ui.theme.TextPrimary
import com.playermp3.ui.theme.TextSecondary

@Composable
fun MiniPlayer(
    track: AudioTrack,
    isPlaying: Boolean,
    shuffleOn: Boolean,
    repeatOn: Boolean,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        GlassSurface(corner = 28.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundControl(
                    icon = Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    size = 40.dp,
                    padding = 20.dp,
                    color = if (shuffleOn) TextPrimary else TextSecondary,
                    onClick = onToggleShuffle,
                )
                RoundControl(
                    icon = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    size = 46.dp,
                    padding = 24.dp,
                    color = TextPrimary,
                    onClick = onPrevious,
                )
                StreamBigButton(
                    isPlaying = isPlaying,
                    size = 58.dp,
                    onClick = onTogglePlay,
                )
                RoundControl(
                    icon = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    size = 46.dp,
                    padding = 24.dp,
                    color = TextPrimary,
                    onClick = onNext,
                )
                RoundControl(
                    icon = Icons.Filled.Repeat,
                    contentDescription = "Repeat",
                    size = 40.dp,
                    padding = 20.dp,
                    color = if (repeatOn) TextPrimary else TextSecondary,
                    onClick = onToggleRepeat,
                )
            }
        }
    }
}

private fun tabIcon(tab: HomeTab): ImageVector = when (tab) {
    HomeTab.Library -> Icons.Filled.LibraryMusic
    HomeTab.Search -> Icons.Filled.Search
    HomeTab.Albums -> Icons.Filled.Album
    HomeTab.Settings -> Icons.Filled.Settings
}

@Composable
fun BottomTabBar(
    selected: HomeTab,
    onSelect: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(DeepNavy),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HomeTab.entries.forEach { tab ->
            val active = selected == tab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = tabIcon(tab),
                    contentDescription = tab.label,
                    tint = if (active) TextPrimary else TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.75f),
                    color = if (active) TextPrimary else TextSecondary,
                )
            }
        }
    }
}