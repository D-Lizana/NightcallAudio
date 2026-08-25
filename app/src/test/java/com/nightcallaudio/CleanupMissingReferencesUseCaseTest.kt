package com.nightcallaudio

import com.nightcallaudio.domain.model.Playlist
import com.nightcallaudio.domain.repository.FavoritesRepository
import com.nightcallaudio.domain.repository.PlaylistRepository
import com.nightcallaudio.domain.usecase.CleanupMissingReferencesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CleanupMissingReferencesUseCaseTest {
    @Test
    fun `envia el mismo conjunto valido a playlists y favoritos`() = runBlocking {
        val playlists = RecordingPlaylistRepository()
        val favorites = RecordingFavoritesRepository()
        val validIds = setOf(1L, 3L, 8L)

        CleanupMissingReferencesUseCase(playlists, favorites)(validIds)

        assertEquals(validIds, playlists.cleanedWith)
        assertEquals(validIds, favorites.cleanedWith)
    }

    private class RecordingPlaylistRepository : PlaylistRepository {
        var cleanedWith: Set<Long>? = null
        override fun observePlaylists(): Flow<List<Playlist>> = flowOf(emptyList())
        override suspend fun create(name: String) = 1L
        override suspend fun rename(playlistId: Long, name: String) = Unit
        override suspend fun delete(playlistId: Long) = Unit
        override suspend fun addTrack(playlistId: Long, trackId: Long) = true
        override suspend fun removeTrack(playlistId: Long, trackId: Long) = true
        override suspend fun moveTrack(playlistId: Long, fromIndex: Int, toIndex: Int) = Unit
        override suspend fun removeMissingTracks(validTrackIds: Set<Long>) { cleanedWith = validTrackIds }
    }

    private class RecordingFavoritesRepository : FavoritesRepository {
        var cleanedWith: Set<Long>? = null
        override fun observeFavoriteIds(): Flow<Set<Long>> = flowOf(emptySet())
        override suspend fun setFavorite(trackId: Long, favorite: Boolean) = Unit
        override suspend fun removeMissing(validTrackIds: Set<Long>) { cleanedWith = validTrackIds }
    }
}
