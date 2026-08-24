package com.nightcallaudio.domain.repository

import com.nightcallaudio.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun observePlaylists(): Flow<List<Playlist>>
    suspend fun create(name: String): Long
    suspend fun rename(playlistId: Long, name: String)
    suspend fun delete(playlistId: Long)
    suspend fun addTrack(playlistId: Long, trackId: Long): Boolean
    suspend fun removeTrack(playlistId: Long, trackId: Long): Boolean
    suspend fun moveTrack(playlistId: Long, fromIndex: Int, toIndex: Int)
    suspend fun removeMissingTracks(validTrackIds: Set<Long>)
}
