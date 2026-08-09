package com.playermp3.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.playermp3.data.AudioTrack
import com.playermp3.playback.PlayerUiState
import com.playermp3.ui.RoundControl
import com.playermp3.ui.TrackRow
import com.playermp3.ui.theme.TextPrimary
import com.playermp3.ui.theme.TextSecondary
import com.playermp3.ui.theme.TextTertiary

@Composable
fun SearchScreen(
    ui: PlayerUiState,
    onBack: () -> Unit,
    onPlay: (AudioTrack, List<AudioTrack>) -> Unit,
) {
    val songs = ui.library?.allSongs ?: emptyList()
    var query by remember { mutableStateOf("") }

    val results = remember(query, songs) {
        if (query.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundControl(
                icon = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                color = TextPrimary,
                size = 44.dp,
                padding = 26.dp,
                onClick = onBack,
            )
            Text(
                text = "Search",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        TextField(
            value = query,
            onValueChange = { query = it },
            placeholder = {
                Text("Songs, artists, albums", color = TextTertiary)
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0x14FFFFFF),
                unfocusedContainerColor = Color(0x14FFFFFF),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = TextPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
        )
        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No matches for \u201C$query\u201D",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = results,
                    key = { it.id },
                ) { track ->
                    TrackRow(
                        track = track,
                        onClick = { onPlay(track, songs) },
                    )
                }
            }
        }
    }
}