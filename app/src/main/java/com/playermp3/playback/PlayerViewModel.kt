package com.playermp3.playback

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.PlaybackParameters
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.playermp3.data.AudioRepository
import com.playermp3.data.AudioTrack
import com.playermp3.data.AppSettings
import com.playermp3.data.LibraryData
import com.playermp3.data.SettingsStore
import com.playermp3.ui.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlayerUiState(
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val hasPermission: Boolean = false,
    val library: LibraryData? = null,
    val currentTrack: AudioTrack? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleOn: Boolean = false,
    val repeatAll: Boolean = false,
    val recentlyPlayed: List<AudioTrack> = emptyList(),
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AudioRepository(application)
    private val settingsStore = SettingsStore(application)

    private val _ui = MutableStateFlow(PlayerUiState())
    val ui: StateFlow<PlayerUiState> = _ui.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsStore.settings

    private var controller: MediaController? = null
    private var connecting = false
    private var tickerJob: Job? = null
    private var pendingPlay: Pair<AudioTrack, List<AudioTrack>>? = null
    private var pendingSingle: AudioTrack? = null

    // ------------------------------------------------------------------
    // Connectivity & ticking
    // ------------------------------------------------------------------

    private fun safePublish(c: MediaController) {
        try {
            publish(c)
        } catch (_: Exception) {
        }
    }

    fun connect() {
        if (controller != null || connecting) return
        connecting = true
        val token = SessionToken(
            getApplication(),
            ComponentName(getApplication(), PlayerService::class.java)
        )
        val future = MediaController.Builder(getApplication(), token).buildAsync()
        future.addListener(
            {
                try {
                    val c = future.get()
                    this@PlayerViewModel.controller = c
                    c.setPlaybackParameters(
                        PlaybackParameters(settingsStore.settings.value.playbackSpeed, 1f)
                    )
                    val saved = settingsStore.settings.value
                    try {
                        c.repeatMode = if (saved.repeatAll) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
                        c.shuffleModeEnabled = saved.shuffle
                    } catch (_: Exception) {
                    }
                    c.addListener(object : Player.Listener {
                        override fun onEvents(player: Player, events: Player.Events) {
                            safePublish(c)
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            _ui.update { it.copy(currentTrack = null, isPlaying = false) }
                        }
                    })
                    tickerJob = viewModelScope.launch {
                        while (true) {
                            safePublish(c)
                            delay(500L)
                        }
                    }
                    pendingPlay?.let { (track, queue) ->
                        pendingPlay = null
                        playTrack(track, queue)
                    }
                    pendingSingle?.let { track ->
                        pendingSingle = null
                        playTrackSingle(track)
                    }
                } catch (_: Exception) {
                    connecting = false
                }
            },
            // MediaController methods must be called from the application's
            // main thread (the looper that created it), so the connect
            // callback runs on the main executor instead of a worker thread.
            ContextCompat.getMainExecutor(getApplication())
        )
    }

    private fun publish(c: MediaController) {
        val state = _ui.value
        val mediaItem = c.currentMediaItem
        val library = state.library

        val currentTrack = mediaItem?.let { item ->
            val mediaId = item.mediaId.toLongOrNull()
            library?.allSongs?.firstOrNull { it.id == mediaId }
                ?: finalizeFromMediaItem(item)
        }

        _ui.update {
            it.copy(
                currentTrack = currentTrack,
                isPlaying = c.isPlaying,
                positionMs = c.currentPosition.coerceAtLeast(0L),
                durationMs = c.duration.takeIf { d -> d > 0L }
                    ?: currentTrack?.durationMs
                    ?: 0L,
                shuffleOn = c.shuffleModeEnabled,
                repeatAll = c.repeatMode == Player.REPEAT_MODE_ALL,
            )
        }
    }

    // ------------------------------------------------------------------
    // Library & permissions
    // ------------------------------------------------------------------

    fun setPermission(granted: Boolean) {
        if (_ui.value.hasPermission == granted) return
        _ui.update { it.copy(hasPermission = granted) }
        if (granted) loadLibrary()
    }

    fun loadLibrary() {
        if (_ui.value.loaded && _ui.value.library != null) return
        refreshLibrary()
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true) }
            val data = repository.load(settingsStore.settings.value.folders.toList())
            _ui.update { it.copy(loading = false, loaded = true, library = data) }
        }
    }

    // ------------------------------------------------------------------
    // Settings
    // ------------------------------------------------------------------

    fun setThemeMode(mode: ThemeMode) {
        settingsStore.setThemeMode(mode)
    }

    fun addFolder(uriString: String) {
        settingsStore.addFolder(uriString)
        viewModelScope.launch {
            _ui.update { it.copy(loading = true) }
            val data = repository.load(settingsStore.settings.value.folders.toList())
            _ui.update { it.copy(loading = false, loaded = true, library = data) }
        }
    }

    fun clearFolders() {
        settingsStore.clearFolders()
        refreshLibrary()
    }

    fun cyclePlaybackSpeed() {
        val speed = settingsStore.cyclePlaybackSpeed()
        controller?.setPlaybackParameters(PlaybackParameters(speed, 1f))
    }

    fun setEqualizer(on: Boolean) {
        settingsStore.setEqualizer(on)
    }

    fun setGapless(on: Boolean) {
        settingsStore.setGapless(on)
    }

    // ------------------------------------------------------------------
    // Playback controls
    // ------------------------------------------------------------------

    fun playTrack(track: AudioTrack, queue: List<AudioTrack>) {
        pendingPlay = track to queue
        val c = controller
        if (c != null) {
            startPlayback(c, track, queue)
        } else {
            connect()
        }
    }

    fun playTrackSingle(track: AudioTrack) {
        pendingSingle = track
        val c = controller
        if (c != null) {
            startSingle(c, track)
        } else {
            connect()
        }
    }

    private fun startPlayback(c: MediaController, track: AudioTrack, queue: List<AudioTrack>) {
        try {
            val items = queue.map { it.toMediaItem() }
            if (items.isEmpty()) return
            val startIndex = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
            c.setMediaItems(items, startIndex, 0L)
            c.prepare()
            c.play()
            recordRecent(track)
        } catch (_: Exception) {
            // Never let a session hiccup take down the UI.
        }
    }

    private fun startSingle(c: MediaController, track: AudioTrack) {
        try {
            c.setMediaItem(track.toMediaItem())
            c.prepare()
            c.play()
            recordRecent(track)
        } catch (_: Exception) {
        }
    }

    fun togglePlay() {
        try {
            val c = controller ?: return
            if (c.isPlaying) c.pause() else c.play()
        } catch (_: Exception) {
        }
    }

    fun next() {
        try {
            controller?.seekToNextMediaItem()
        } catch (_: Exception) {
        }
    }

    fun previous() {
        try {
            val c = controller ?: return
            if (c.currentPosition > 3_000L) {
                c.seekTo(0L)
            } else {
                c.seekToPreviousMediaItem()
            }
        } catch (_: Exception) {
        }
    }

    fun seekTo(ms: Long) {
        try {
            controller?.seekTo(ms.coerceIn(0L, _ui.value.durationMs))
        } catch (_: Exception) {
        }
    }

    fun toggleShuffle() {
        try {
            val c = controller
            val next = if (c != null) !c.shuffleModeEnabled
            else !settingsStore.settings.value.shuffle
            settingsStore.setShuffle(next)
            controller?.shuffleModeEnabled = next
        } catch (_: Exception) {
        }
    }

    fun toggleRepeat() {
        try {
            val c = controller ?: return
            val next = if (c.repeatMode == Player.REPEAT_MODE_ALL) Player.REPEAT_MODE_OFF
            else Player.REPEAT_MODE_ALL
            settingsStore.setRepeatAll(next == Player.REPEAT_MODE_ALL)
            controller?.repeatMode = next
        } catch (_: Exception) {
        }
    }

    fun stopPlayback() {
        try {
            val c = controller ?: return
            c.stop()
            c.clearMediaItems()
        } catch (_: Exception) {
        }
        _ui.update {
            it.copy(
                currentTrack = null,
                isPlaying = false,
                positionMs = 0L,
                durationMs = 0L,
            )
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        tickerJob = null
        try {
            controller?.release()
        } catch (_: Exception) {
        }
        controller = null
        connecting = false
        super.onCleared()
    }

    internal fun finalizeFromMediaItem(item: MediaItem): AudioTrack {
        val md = item.mediaMetadata
        return AudioTrack(
            id = item.mediaId.toLongOrNull() ?: 0L,
            title = md.title?.toString() ?: "Unknown",
            artist = md.artist?.toString() ?: "Unknown Artist",
            album = md.albumTitle?.toString() ?: "Unknown Album",
            albumId = 0L,
            durationMs = item.mediaMetadata.durationMs ?: 0L,
            uri = item.requestMetadata.mediaUri?.toString()
                ?: item.localConfiguration?.uri?.toString()
                ?: "",
        )
    }

    private fun recordRecent(track: AudioTrack) {
        _ui.update { state ->
            val recent = (listOf(track) + state.recentlyPlayed.filter { it.id != track.id })
                .take(12)
            state.copy(recentlyPlayed = recent)
        }
    }
}

private fun AudioTrack.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(artworkUri)
                .setDurationMs(durationMs)
                .build()
        )
        .build()