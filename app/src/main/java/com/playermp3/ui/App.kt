package com.playermp3.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.playermp3.data.Album
import com.playermp3.playback.PlayerViewModel
import com.playermp3.ui.screens.AlbumDetailScreen
import com.playermp3.ui.screens.AlbumsScreen
import com.playermp3.ui.screens.LibraryScreen
import com.playermp3.ui.screens.NowPlayingScreen
import com.playermp3.ui.screens.SearchScreen
import com.playermp3.ui.screens.SettingsScreen
import com.playermp3.ui.theme.Night

enum class HomeTab(val label: String) {
    Library("Library"),
    Search("Search"),
    Albums("Albums"),
    Settings("Settings"),
}

sealed interface AppScreen {
    data class Tab(val tab: HomeTab) : AppScreen
    data class AlbumDetail(val album: Album) : AppScreen
    data object NowPlaying : AppScreen
}

@Composable
fun CadenceApp(viewModel: PlayerViewModel) {
    val ui by viewModel.ui.collectAsState()

    var stack by remember { mutableStateOf(listOf<AppScreen>(AppScreen.Tab(HomeTab.Library))) }
    val current = stack.last()

    fun navigate(screen: AppScreen) {
        stack = stack + screen
    }

    fun pop() {
        if (stack.size > 1) stack = stack.dropLast(1)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.setPermission(result.values.any { it })
    }

    LaunchedEffect(Unit) {
        viewModel.connect()
    }

    LaunchedEffect(ui.hasPermission) {
        if (ui.hasPermission) {
            viewModel.loadLibrary()
        } else {
            val permissions = if (Build.VERSION.SDK_INT >= 33) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissionLauncher.launch(permissions)
        }
    }

    BackHandler(enabled = stack.size > 1) {
        pop()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Night)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (val screen = current) {
                is AppScreen.Tab -> when (screen.tab) {
                    HomeTab.Library -> LibraryScreen(
                        ui = ui,
                        onPlay = { track, queue ->
                            viewModel.playTrack(track, queue)
                            navigate(AppScreen.NowPlaying)
                        },
                        onAlbum = { album -> navigate(AppScreen.AlbumDetail(album)) },
                        onOpenSearch = { navigate(AppScreen.Tab(HomeTab.Search)) },
                        onRefresh = { viewModel.refreshLibrary() },
                    )

                    HomeTab.Search -> SearchScreen(
                        ui = ui,
                        onPlay = { track, queue ->
                            viewModel.playTrack(track, queue)
                            navigate(AppScreen.NowPlaying)
                        },
                    )

                    HomeTab.Albums -> AlbumsScreen(
                        ui = ui,
                        onAlbum = { album -> navigate(AppScreen.AlbumDetail(album)) },
                    )

                    HomeTab.Settings -> SettingsScreen(
                        ui = ui,
                        onRefresh = { viewModel.refreshLibrary() },
                    )
                }

                is AppScreen.AlbumDetail -> AlbumDetailScreen(
                    album = screen.album,
                    ui = ui,
                    onBack = { pop() },
                    onPlay = { track, queue ->
                        viewModel.playTrack(track, queue)
                        navigate(AppScreen.NowPlaying)
                    },
                )

                AppScreen.NowPlaying -> NowPlayingScreen(
                    ui = ui,
                    onBack = { pop() },
                    onTogglePlay = { viewModel.togglePlay() },
                    onNext = { viewModel.next() },
                    onPrevious = { viewModel.previous() },
                    onSeekTo = { ms -> viewModel.seekTo(ms) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onToggleRepeat = { viewModel.toggleRepeat() },
                )
            }
        }

        val track = ui.currentTrack
        if (track != null && current != AppScreen.NowPlaying) {
            MiniPlayer(
                track = track,
                isPlaying = ui.isPlaying,
                shuffleOn = ui.shuffleOn,
                repeatOn = ui.repeatAll,
                onTogglePlay = { viewModel.togglePlay() },
                onNext = { viewModel.next() },
                onPrevious = { viewModel.previous() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onClick = { navigate(AppScreen.NowPlaying) },
            )
        }

        val currentTab = (current as? AppScreen.Tab)?.tab
        if (currentTab != null) {
            BottomTabBar(
                selected = currentTab,
                onSelect = { tab ->
                    val top = stack.lastOrNull()
                    if (top is AppScreen.Tab) {
                        stack = stack.dropLast(1) + AppScreen.Tab(tab)
                    } else {
                        navigate(AppScreen.Tab(tab))
                    }
                },
                modifier = Modifier.navigationBarsPadding(),
            )
        }
    }
}