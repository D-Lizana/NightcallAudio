package com.nightcallaudio

import com.nightcallaudio.domain.model.RepeatMode
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.repository.PersistedPlaybackSession
import com.nightcallaudio.domain.usecase.RestorePlaybackSessionUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RestorePlaybackSessionUseCaseTest {
    private val useCase = RestorePlaybackSessionUseCase()
    private val first = track(1)
    private val second = track(2)

    @Test
    fun `conserva duplicados y elimina referencias desaparecidas`() {
        val result = useCase(
            session = session(trackIds = listOf(1, 99, 2, 1), currentIndex = 3, positionMs = 42_000),
            availableTracks = listOf(first, second),
        )!!

        assertEquals(listOf(1L, 2L, 1L), result.tracks.map(Track::id))
        assertEquals(listOf(0, 2, 3), result.originalPositions)
        assertEquals(2, result.currentIndex)
        assertEquals(42_000L, result.positionMs)
    }

    @Test
    fun `si desaparece la pista actual elige la siguiente y reinicia posicion`() {
        val result = useCase(
            session = session(trackIds = listOf(1, 99, 2), currentIndex = 1, positionMs = 42_000),
            availableTracks = listOf(first, second),
        )!!

        assertEquals(1, result.currentIndex)
        assertEquals(second, result.tracks[result.currentIndex])
        assertEquals(0L, result.positionMs)
    }

    @Test
    fun `devuelve nulo si no queda ningun archivo disponible`() {
        assertNull(useCase(session(listOf(99), 0, 10), listOf(first, second)))
    }

    private fun session(trackIds: List<Long>, currentIndex: Int, positionMs: Long) = PersistedPlaybackSession(
        trackIds = trackIds,
        originalPositions = trackIds.indices.toList(),
        currentIndex = currentIndex,
        positionMs = positionMs,
        shuffleEnabled = true,
        repeatMode = RepeatMode.ALL,
    )

    private fun track(id: Long) = Track(
        id = id,
        contentUri = "content://audio/$id",
        title = "Canción $id",
        artist = "Artista",
        artistId = 1,
        album = "Álbum",
        albumId = 1,
        artworkUri = null,
        durationMs = 180_000,
        trackNumber = id.toInt(),
        discNumber = 1,
        genre = null,
        folder = "Music",
        year = 2026,
        dateAddedEpochSeconds = 1,
    )
}
