package com.playermp3.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.playermp3.data.Album
import com.playermp3.data.AppSettings
import com.playermp3.data.AudioTrack
import com.playermp3.playback.PlayerUiState
import com.playermp3.playback.PlayerViewModel
import com.playermp3.ui.screens.AlbumDetailScreen
import com.playermp3.ui.screens.AlbumsScreen
import com.playermp3.ui.screens.LibraryScreen
import com.playermp3.ui.screens.NowPlayingScreen
import com.playermp3.ui.screens.SearchScreen
import com.playermp3.ui.screens.SettingsScreen
import com.playermp3.ui.theme.LocalAppDesign
import com.playermp3.ui.theme.ThemeMode

enum class HomeTab(val label: String) {
    Library("Library"),
    Search("Search"),
    Albums("Album"),
    Settings("Settings");

    companion object {
        val mainTabs = listOf(Library, Albums, Settings)
    }
}

sealed interface AppScreen {
    data class Tab(val tab: HomeTab) : AppScreen
    data class AlbumDetail(val album: Album) : AppScreen
    data object NowPlaying : AppScreen
}

@Composable
fun CadenceApp(viewModel: PlayerViewModel) {
    val ui by viewModel.ui.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val design = LocalAppDesign.current

    // The three main tabs live in a horizontal pager; Album detail, search
    // and the now-playing screen are pushed on top.
    var selectedTab by remember { mutableStateOf(HomeTab.Library) }
    var pushed by remember { mutableStateOf(listOf<AppScreen>()) }
    val current = pushed.lastOrNull()

    fun navigate(screen: AppScreen) {
        pushed = pushed + screen
    }

    fun openTab(tab: HomeTab) {
        if (tab == HomeTab.Search) {
            navigate(AppScreen.Tab(tab))
        } else {
            selectedTab = tab
            pushed = emptyList()
        }
    }

    fun pop() {
        pushed = if (pushed.size > 1) pushed.dropLast(1) else emptyList()
    }

    val context = LocalContext.current
    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            viewModel.addFolder(uri.toString())
        }
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

    BackHandler(enabled = pushed.isNotEmpty()) {
        pop()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(design.background))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // The main tab scaffold is ALWAYS composed. Pushed screens
            // (album detail, search, now playing) are drawn as opaque
            // overlays on top, so collapsing a screen never disposes and
            // re-mounts the pager underneath.
            MainTabScaffold(
                selectedTab = selectedTab,
                onSelectTab = { openTab(it) },
                ui = ui,
                settings = settings,
                onPlay = { track, queue ->
                    viewModel.playTrack(track, queue)
                    navigate(AppScreen.NowPlaying)
                },
                onAlbum = { album -> navigate(AppScreen.AlbumDetail(album)) },
                onOpenSearch = { navigate(AppScreen.Tab(HomeTab.Search)) },
                onRefresh = { viewModel.refreshLibrary() },
                onThemeMode = { viewModel.setThemeMode(it) },
                onPickFolders = { folderLauncher.launch(null) },
                onClearFolders = { viewModel.clearFolders() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onCycleSpeed = { viewModel.cyclePlaybackSpeed() },
                onToggleEqualizer = { viewModel.setEqualizer(!settings.equalizer) },
                onToggleGapless = { viewModel.setGapless(!settings.gapless) },
                onOpenNowPlaying = {
                    if (ui.currentTrack != null) navigate(AppScreen.NowPlaying)
                },
            )

            val overlay = pushed.lastOrNull()
            if (overlay != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(design.background)),
                ) {
                    when (overlay) {
                        is AppScreen.Tab -> SearchScreen(
                            ui = ui,
                            onPlay = { track, queue ->
                                viewModel.playTrack(track, queue)
                                navigate(AppScreen.NowPlaying)
                            },
                        )

                        is AppScreen.AlbumDetail -> AlbumDetailScreen(
                            album = overlay.album,
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
            }
        }

        val track = ui.currentTrack
        if (track != null && current !is AppScreen.NowPlaying) {
            MiniPlayer(
                track = track,
                isPlaying = ui.isPlaying,
                positionMs = ui.positionMs,
                durationMs = ui.durationMs,
                onTogglePlay = { viewModel.togglePlay() },
                onNext = { viewModel.next() },
                onClick = { navigate(AppScreen.NowPlaying) },
            )
        }
    }
}

@Composable
private fun MainTabScaffold(
    selectedTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit,
    ui: PlayerUiState,
    settings: AppSettings,
    onPlay: (AudioTrack, List<AudioTrack>) -> Unit,
    onAlbum: (Album) -> Unit,
    onOpenSearch: () -> Unit,
    onRefresh: () -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onPickFolders: () -> Unit,
    onClearFolders: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleEqualizer: () -> Unit,
    onToggleGapless: () -> Unit,
    onOpenNowPlaying: () -> Unit,
) {
    val design = LocalAppDesign.current
    val pages = HomeTab.mainTabs
    val pagerState = rememberPagerState(
        initialPage = pages.indexOf(selectedTab).coerceAtLeast(0)
    ) { pages.size }

    LaunchedEffect(selectedTab) {
        val target = pages.indexOf(selectedTab)
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }
    LaunchedEffect(pagerState.settledPage) {
        val settled = pages.getOrNull(pagerState.settledPage)
        if (settled != null && settled != selectedTab) {
            onSelectTab(settled)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Cadence",
                    style = MaterialTheme.typography.titleLarge,
                    color = design.text,
                )
                Text(
                    text = "Music player",
                    style = MaterialTheme.typography.bodySmall,
                    color = design.textSecondary,
                )
            }
            RoundControl(
                icon = Icons.Filled.Search,
                contentDescription = "Search",
                color = design.text,
                onClick = onOpenSearch,
            )
        }

        TopNav(selected = selectedTab, onSelect = { onSelectTab(it) })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            when (pages[page]) {
                HomeTab.Library -> LibraryScreen(
                    ui = ui,
                    onPlay = onPlay,
                    onRefresh = onRefresh,
                )

                HomeTab.Albums -> AlbumsScreen(
                    ui = ui,
                    onAlbum = onAlbum,
                )

                HomeTab.Settings -> SettingsScreen(
                    ui = ui,
                    settings = settings,
                    onThemeMode = onThemeMode,
                    onPickFolders = onPickFolders,
                    onClearFolders = onClearFolders,
                    onToggleRepeat = onToggleRepeat,
                    onToggleShuffle = onToggleShuffle,
                    onCycleSpeed = onCycleSpeed,
                    onToggleEqualizer = onToggleEqualizer,
                    onToggleGapless = onToggleGapless,
                    onOpenNowPlaying = onOpenNowPlaying,
                )

                HomeTab.Search -> Box(modifier = Modifier.height(1.dp))
            }
        }
    }
}
