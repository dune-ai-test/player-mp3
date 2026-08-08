package com.playermp3.playback

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.playermp3.data.AudioRepository
import com.playermp3.data.AudioTrack
import com.playermp3.data.LibraryData
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
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

    private val _ui = MutableStateFlow(PlayerUiState())
    val ui: StateFlow<PlayerUiState> = _ui.asStateFlow()

    private var controller: MediaController? = null
    private var connectJob: kotlinx.coroutines.Job? = null

    // ------------------------------------------------------------------
    // Connectivity & ticking
    // ------------------------------------------------------------------

    fun connect() {
        if (controller != null || connectJob?.isActive == true) return
        connectJob = viewModelScope.launch {
            val token = SessionToken(
                getApplication(),
                ComponentName(getApplication(), PlayerService::class.java)
            )
            val future: ListenableFuture<MediaController> =
                MediaController.Builder(getApplication(), token).buildAsync()
            try {
                val c = future.await()
                this@PlayerViewModel.controller = c
                c.addListener(object : Player.Listener {
                    override fun onEvents(player: Player, events: Player.Events) {
                        publish(c)
                    }
                })
                viewModelScope.launch {
                    while (true) {
                        publish(c)
                        delay(500L)
                    }
                }
            } catch (_: Exception) {
                // Connection will be re-attempted on the next connect() call.
            }
        }
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
        viewModelScope.launch {
            _ui.update { it.copy(loading = true) }
            val data = repository.load()
            _ui.update { it.copy(loading = false, loaded = true, library = data) }
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true) }
            val data = repository.load()
            _ui.update { it.copy(loading = false, loaded = true, library = data) }
        }
    }

    // ------------------------------------------------------------------
    // Playback controls
    // ------------------------------------------------------------------

    fun playTrack(track: AudioTrack, queue: List<AudioTrack>) {
        val c = controller ?: return
        val items = queue.map { it.toMediaItem() }
        val startIndex = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        if (items.isEmpty()) return

        c.setMediaItems(items, startIndex, 0L)
        c.prepare()
        c.play()
        recordRecent(track)
    }

    fun playTrackSingle(track: AudioTrack) {
        val c = controller ?: return
        c.setMediaItem(track.toMediaItem())
        c.prepare()
        c.play()
        recordRecent(track)
    }

    fun togglePlay() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun previous() {
        val c = controller ?: return
        if (c.currentPosition > 3_000L) {
            c.seekTo(0L)
        } else {
            c.seekToPreviousMediaItem()
        }
    }

    fun seekTo(ms: Long) {
        controller?.seekTo(ms.coerceIn(0L, _ui.value.durationMs))
    }

    fun toggleShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    fun toggleRepeat() {
        val c = controller ?: return
        c.repeatMode =
            if (c.repeatMode == Player.REPEAT_MODE_ALL) Player.REPEAT_MODE_OFF
            else Player.REPEAT_MODE_ALL
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