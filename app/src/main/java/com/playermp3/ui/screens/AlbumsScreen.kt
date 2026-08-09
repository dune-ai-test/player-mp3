package com.playermp3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.playermp3.data.Album
import com.playermp3.playback.PlayerUiState
import com.playermp3.ui.AlbumArt
import com.playermp3.ui.HomeTab
import com.playermp3.ui.SectionHeader
import com.playermp3.ui.TopNav
import com.playermp3.ui.theme.TextPrimary
import com.playermp3.ui.theme.TextSecondary

@Composable
fun AlbumsScreen(
    ui: PlayerUiState,
    selectedTab: HomeTab,
    onTabSelect: (HomeTab) -> Unit,
    onAlbum: (Album) -> Unit,
) {
    val albums = ui.library?.albums ?: emptyList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 0.dp,
            bottom = 20.dp,
        ),
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            TopNav(selected = selectedTab, onSelect = onTabSelect)
        }
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            SectionHeader(
                text = "Albums",
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        items(
            items = albums,
            key = { it.albumId },
        ) { album ->
            AlbumCard(album = album, onClick = { onAlbum(album) })
        }
    }
}

@Composable
private fun AlbumCard(
    album: Album,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        AlbumArt(
            artworkUri = album.artworkUri?.toString(),
            albumTitle = album.name,
            corner = 22.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = album.name,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
        )
        Text(
            text = "${album.tracks.size} songs · ${album.artist}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1,
        )
    }
}