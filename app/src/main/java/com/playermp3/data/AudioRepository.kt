package com.playermp3.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AudioTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val uri: String,
) {
    val artworkUri: Uri?
        get() = if (albumId > 0) {
            try {
                ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )
            } catch (_: Exception) {
                null
            }
        } else null
}

data class Album(
    val albumId: Long,
    val name: String,
    val artist: String,
    val tracks: List<AudioTrack>,
) {
    val artworkUri: Uri?
        get() = if (albumId > 0) {
            try {
                ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )
            } catch (_: Exception) {
                null
            }
        } else null
}

data class LibraryData(
    val allSongs: List<AudioTrack>,
    val albums: List<Album>,
)

class AudioRepository(private val context: Context) {

    fun artUri(albumId: Long): Uri? =
        if (albumId > 0) {
            try {
                ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )
            } catch (_: Exception) {
                null
            }
        } else null

    suspend fun load(): LibraryData = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val songs = mutableListOf<AudioTrack>()

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idxId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val idxTitle = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val idxArtist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val idxAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val idxAlbumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val idxDuration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idxId)
                songs += AudioTrack(
                    id = id,
                    title = cursor.getString(idxTitle) ?: "Unknown",
                    artist = cursor.getString(idxArtist)?.takeIf { it.isNotBlank() } ?: "Unknown Artist",
                    album = cursor.getString(idxAlbum)?.takeIf { it.isNotBlank() } ?: "Unknown Album",
                    albumId = cursor.getLong(idxAlbumId),
                    durationMs = if (cursor.isNull(idxDuration)) 0L else cursor.getLong(idxDuration),
                    uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString(),
                )
            }
        }

        val albums = songs
            .groupBy { it.albumId }
            .map { (albumId, tracks) ->
                val first = tracks.first()
                Album(
                    albumId = albumId,
                    name = first.album,
                    artist = first.artist,
                    tracks = tracks,
                )
            }
            .sortedBy { it.name.lowercase() }

        LibraryData(allSongs = songs, albums = albums)
    }
}