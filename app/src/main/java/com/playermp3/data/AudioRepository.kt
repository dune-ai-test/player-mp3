package com.playermp3.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
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

    /**
     * Loads the library. When [folderUris] is non-empty, only music files under
     * those picked folders are included; otherwise the whole device is scanned.
     */
    suspend fun load(folderUris: List<String> = emptyList()): LibraryData =
        withContext(Dispatchers.IO) {
            val prefixes = folderUris.mapNotNull { resolvePrefix(it) }
            val filtered = prefixes.isNotEmpty()

            val projection = buildList {
                add(MediaStore.Audio.Media._ID)
                add(MediaStore.Audio.Media.TITLE)
                add(MediaStore.Audio.Media.ARTIST)
                add(MediaStore.Audio.Media.ALBUM)
                add(MediaStore.Audio.Media.ALBUM_ID)
                add(MediaStore.Audio.Media.DURATION)
                if (Build.VERSION.SDK_INT >= 29) {
                    add(MediaStore.Audio.Media.RELATIVE_PATH)
                }
                add(MediaStore.Audio.Media.DATA)
            }.toTypedArray()

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
                val idxRelative = cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                val idxData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    if (filtered) {
                        val relative = cursor.getString(idxRelative)
                        val data = cursor.getString(idxData)
                        if (!underPrefixes(relative, data, prefixes)) continue
                    }

                    val id = cursor.getLong(idxId)
                    songs += AudioTrack(
                        id = id,
                        title = cursor.getString(idxTitle) ?: "Unknown",
                        artist = cursor.getString(idxArtist)?.takeIf { it.isNotBlank() }
                            ?: "Unknown Artist",
                        album = cursor.getString(idxAlbum)?.takeIf { it.isNotBlank() }
                            ?: "Unknown Album",
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

    private fun resolvePrefix(treeUri: String): String? = runCatching {
        val uri = Uri.parse(treeUri)
        val docId = DocumentsContract.getTreeDocumentId(uri)
        val prefix = docId.substringAfter(':', "").trim('/')
        prefix.takeIf { it.isNotBlank() }
    }.getOrNull()

    private fun underPrefixes(relative: String?, data: String?, prefixes: List<String>): Boolean {
        var path = relative?.trim('/')
        if (path.isNullOrBlank() && !data.isNullOrBlank()) {
            val trimmed = data.trim('/')
            val marker = "emulated/0/"
            val idx = trimmed.indexOf(marker)
            path = if (idx >= 0) trimmed.substring(idx + marker.length).trim('/') else null
            if (path.isNullOrBlank()) return false
        }
        path = path ?: return false
        return prefixes.any { prefix ->
            path == prefix || path.startsWith("$prefix/")
        }
    }
}