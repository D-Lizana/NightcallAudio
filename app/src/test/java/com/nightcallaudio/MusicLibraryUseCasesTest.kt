package com.nightcallaudio

import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.usecase.GetMusicLibraryUseCase
import com.nightcallaudio.domain.usecase.SearchTracksUseCase
import com.nightcallaudio.testutil.FakeMusicRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MusicLibraryUseCasesTest {
    private val tracks = listOf(
        track(1, "Midnight", "Kavinsky", 10, "OutRun", 20, "Synthwave", "Music"),
        track(2, "Nightcall", "Kavinsky", 10, "OutRun", 20, "Synthwave", "Music"),
        track(3, "Roadgame", "Kavinsky", 10, "Reborn", 21, null, "Downloads"),
    )

    @Test
    fun `crea agrupaciones ordenadas de la biblioteca`() = runBlocking {
        val library = GetMusicLibraryUseCase(FakeMusicRepository(tracks))()

        assertEquals(listOf("Midnight", "Nightcall", "Roadgame"), library.tracks.map(Track::title))
        assertEquals(1, library.artists.size)
        assertEquals(3, library.artists.single().trackCount)
        assertEquals(listOf("OutRun", "Reborn"), library.albums.map { it.title })
        assertEquals(2, library.folders.size)
        assertEquals("Synthwave", library.genres.single().name)
    }

    @Test
    fun `busca por titulo artista y album sin distinguir mayusculas`() {
        val search = SearchTracksUseCase()

        assertEquals(listOf(2L), search(tracks, "NIGHTCALL").map(Track::id))
        assertEquals(3, search(tracks, "kavinsky").size)
        assertEquals(listOf(3L), search(tracks, "reborn").map(Track::id))
    }

    @Test
    fun `procesa una biblioteca de cientos de canciones sin perder elementos`() = runBlocking {
        val largeLibrary = List(750) { index ->
            tracks.first().copy(
                id = index.toLong(),
                contentUri = "content://audio/$index",
                title = "Canción ${index.toString().padStart(3, '0')}",
                albumId = (index / 15).toLong(),
                album = "Álbum ${index / 15}",
            )
        }

        val result = GetMusicLibraryUseCase(FakeMusicRepository(largeLibrary))()

        assertEquals(750, result.tracks.size)
        assertEquals(50, result.albums.size)
        assertEquals(1, result.artists.size)
    }

    private fun track(
        id: Long,
        title: String,
        artist: String,
        artistId: Long,
        album: String,
        albumId: Long,
        genre: String?,
        folder: String,
    ) = Track(
        id = id,
        contentUri = "content://audio/$id",
        title = title,
        artist = artist,
        artistId = artistId,
        album = album,
        albumId = albumId,
        artworkUri = "content://albumart/$albumId",
        durationMs = 180_000,
        trackNumber = id.toInt(),
        discNumber = 1,
        genre = genre,
        folder = folder,
        year = 2013,
        dateAddedEpochSeconds = 1_700_000_000,
    )
}
