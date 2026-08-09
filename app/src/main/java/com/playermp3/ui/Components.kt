package com.playermp3.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.playermp3.ui.theme.Charcoal
import com.playermp3.ui.theme.GlassBorder
import com.playermp3.ui.theme.GlassCard
import com.playermp3.ui.theme.PaperText
import com.playermp3.ui.theme.TextPrimary
import com.playermp3.ui.theme.TextSecondary
import com.playermp3.ui.theme.TextTertiary
import kotlin.math.absoluteValue

@Composable
fun GlassSurface(
    corner: Dp = 28.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(GlassCard)
            .border(1.dp, GlassBorder, RoundedCornerShape(corner)),
    ) {
        content()
    }
}

@Composable
fun AlbumArt(
    artworkUri: String?,
    albumTitle: String,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    corner: Dp = 14.dp,
) {
    val palette = listOf(
        Color(0xFF1B1E31),
        Color(0xFF312E81),
        Color(0xFF7C3AED),
        Color(0xFF0E7490),
        Color(0xFFB45309),
        Color(0xFF1E3A8A),
        Color(0xFF9D174D),
    )
    val fallback = palette[(albumTitle.hashCode().absoluteValue) % palette.size]
    Box(
        modifier = (if (size != null) modifier.size(size) else modifier)
            .clip(RoundedCornerShape(corner))
            .background(
                Brush.linearGradient(
                    listOf(fallback, fallback.copy(alpha = 0.55f))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (!artworkUri.isNullOrBlank()) {
            AsyncImage(
                model = artworkUri,
                contentDescription = albumTitle,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = albumTitle,
                tint = TextPrimary.copy(alpha = 0.6f),
                modifier = Modifier.size((size ?: 48.dp) * 0.42f),
            )
        }
    }
}

@Composable
fun RoundControl(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = TextPrimary,
    background: Color = Color.Transparent,
    padding: Dp = 10.dp,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(padding),
        )
    }
}

@Composable
fun StreamBigButton(
    isPlaying: Boolean,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Charcoal)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = PaperText,
            modifier = Modifier.size(size * 0.34f),
        )
    }
}

@Composable
fun SectionHeader(
    text: String,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Spacer(Modifier.width(8.dp))
        trailing?.invoke()
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L)) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
fun TrackRow(
    track: com.playermp3.data.AudioTrack,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    number: Int? = null,
    showDuration: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (number != null) {
            Text(
                text = "%02d".format(number),
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                modifier = Modifier.width(28.dp),
            )
        }
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
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
            )
        }
        if (showDuration) {
            Text(
                text = formatTime(track.durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
            )
        }
    }
}