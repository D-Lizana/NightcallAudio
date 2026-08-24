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
        durationMs = 180_000,
        trackNumber = id.toInt(),
        discNumber = 1,
        genre = genre,
        folder = folder,
    )
}
