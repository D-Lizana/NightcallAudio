package com.nightcallaudio.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.repository.MusicRepository
import com.nightcallaudio.domain.usecase.AudioInclusionPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreMusicRepository(
    context: Context,
) : MusicRepository {
    private val resolver = context.applicationContext.contentResolver

    override suspend fun getTracks(): List<Track> = withContext(Dispatchers.IO) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val locationColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.RELATIVE_PATH
        } else {
            MediaStore.Audio.Media.DATA
        }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            locationColumn,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
            "${MediaStore.Audio.Media.DURATION} >= ?"
        val arguments = arrayOf(AudioInclusionPolicy.MINIMUM_DURATION_MS.toString())
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        resolver.query(collection, projection, selection, arguments, sortOrder)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val artistIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val locationIndex = cursor.getColumnIndexOrThrow(locationColumn)

            buildList {
                while (cursor.moveToNext()) {
                    val location = cursor.getString(locationIndex)
                    val durationMs = cursor.getLong(durationIndex)
                    if (!AudioInclusionPolicy.shouldInclude(durationMs, location)) continue

                    val id = cursor.getLong(idIndex)
                    val encodedTrackNumber = cursor.getInt(trackIndex)
                    add(
                        Track(
                            id = id,
                            contentUri = ContentUris.withAppendedId(collection, id).toString(),
                            title = cursor.getString(titleIndex).orUnknown("Título desconocido"),
                            artist = cursor.getString(artistIndex).orUnknown("Artista desconocido"),
                            artistId = cursor.getLong(artistIdIndex).takeIf { it > 0 },
                            album = cursor.getString(albumIndex).orUnknown("Álbum desconocido"),
                            albumId = cursor.getLong(albumIdIndex).takeIf { it > 0 },
                            durationMs = durationMs,
                            trackNumber = encodedTrackNumber.takeIf { it > 0 }?.rem(1_000),
                            discNumber = encodedTrackNumber.takeIf { it >= 1_000 }?.div(1_000),
                            genre = null,
                            folder = location?.folderName(),
                        ),
                    )
                }
            }.distinctBy(Track::contentUri)
        }.orEmpty()
    }

    private fun String.folderName(): String? =
        trimEnd('/', '\\').substringAfterLast('/', substringAfterLast('\\')).takeIf(String::isNotBlank)

    private fun String?.orUnknown(fallback: String): String =
        this?.takeUnless { it.isBlank() || it == MediaStore.UNKNOWN_STRING } ?: fallback

}
