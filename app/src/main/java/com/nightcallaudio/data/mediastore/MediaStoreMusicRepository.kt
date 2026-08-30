package com.nightcallaudio.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.nightcallaudio.R
import com.nightcallaudio.ui.settings.AppLanguageManager
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.repository.MusicRepository
import com.nightcallaudio.domain.usecase.AudioInclusionPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MediaStoreMusicRepository(
    context: Context,
) : MusicRepository {
    private val appContext = context.applicationContext
    private val resolver = context.applicationContext.contentResolver

    override fun observeTracks(): Flow<List<Track>> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer)
        trySend(Unit)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }.conflate()
        .map { getTracks() }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

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
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            locationColumn,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
            "${MediaStore.Audio.Media.DURATION} >= ?"
        val arguments = arrayOf(AudioInclusionPolicy.MINIMUM_DURATION_MS.toString())
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        val genresByTrackId = loadGenresByTrackId()

        resolver.query(collection, projection, selection, arguments, sortOrder)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val artistIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val locationIndex = cursor.getColumnIndexOrThrow(locationColumn)

            buildList {
                while (cursor.moveToNext()) {
                    val location = cursor.getString(locationIndex)
                    val durationMs = cursor.getLong(durationIndex)
                    val album = cursor.getString(albumIndex)
                    if (!AudioInclusionPolicy.shouldInclude(durationMs, location, album)) continue

                    val id = cursor.getLong(idIndex)
                    val albumId = cursor.getLong(albumIdIndex).takeIf { it > 0 }
                    val encodedTrackNumber = cursor.getInt(trackIndex)
                    add(
                        Track(
                            id = id,
                            contentUri = ContentUris.withAppendedId(collection, id).toString(),
                            title = cursor.getString(titleIndex).orUnknown(localizedString(R.string.unknown_title)),
                            artist = cursor.getString(artistIndex).orUnknown(localizedString(R.string.unknown_artist)),
                            artistId = cursor.getLong(artistIdIndex).takeIf { it > 0 },
                            album = album.orUnknown(localizedString(R.string.unknown_album)),
                            albumId = albumId,
                            artworkUri = albumId?.let(::albumArtworkUri),
                            durationMs = durationMs,
                            trackNumber = encodedTrackNumber.takeIf { it > 0 }?.rem(1_000),
                            discNumber = encodedTrackNumber.takeIf { it >= 1_000 }?.div(1_000),
                            genre = genresByTrackId[id],
                            folder = AudioInclusionPolicy.folderName(
                                location = location,
                                isRelativePath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,
                            ),
                            year = cursor.getInt(yearIndex).takeIf { it > 0 },
                            dateAddedEpochSeconds = cursor.getLong(dateAddedIndex).takeIf { it > 0 },
                        ),
                    )
                }
            }.distinctBy(Track::contentUri)
        }.orEmpty()
    }

    private fun localizedString(resourceId: Int) =
        AppLanguageManager.localizedContext(appContext).getString(resourceId)

    private fun loadGenresByTrackId(): Map<Long, String> {
        val genres = mutableMapOf<Long, String>()
        val genreProjection = arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
        runCatching {
            resolver.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                genreProjection,
                null,
                null,
                "${MediaStore.Audio.Genres.NAME} COLLATE NOCASE ASC",
            )?.use { genreCursor ->
                val genreIdIndex = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
                val genreNameIndex = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
                while (genreCursor.moveToNext()) {
                    val genreId = genreCursor.getLong(genreIdIndex)
                    val genreName = genreCursor.getString(genreNameIndex)?.takeIf(String::isNotBlank) ?: continue
                    val membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
                    resolver.query(
                        membersUri,
                        arrayOf(MediaStore.Audio.Genres.Members.AUDIO_ID),
                        null,
                        null,
                        null,
                    )?.use { memberCursor ->
                        val audioIdIndex = memberCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.AUDIO_ID)
                        while (memberCursor.moveToNext()) {
                            genres.putIfAbsent(memberCursor.getLong(audioIdIndex), genreName)
                        }
                    }
                }
            }
        }
        return genres
    }

    private fun albumArtworkUri(albumId: Long): String = ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        albumId,
    ).toString()

    private fun String?.orUnknown(fallback: String): String =
        this?.takeUnless { it.isBlank() || it == MediaStore.UNKNOWN_STRING } ?: fallback

}
