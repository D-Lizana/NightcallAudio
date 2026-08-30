package com.nightcallaudio.data.repository

import com.nightcallaudio.data.database.dao.PlaylistDao
import com.nightcallaudio.data.database.entity.PlaylistEntity
import com.nightcallaudio.domain.model.Playlist
import com.nightcallaudio.ui.settings.DynamicMessages
import com.nightcallaudio.domain.repository.MusicRepository
import com.nightcallaudio.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomPlaylistRepository(
    private val dao: PlaylistDao,
    private val musicRepository: MusicRepository,
    private val now: () -> Long = System::currentTimeMillis,
) : PlaylistRepository {
    override fun observePlaylists(): Flow<List<Playlist>> = combine(
        dao.observeAll(),
        musicRepository.observeTracks(),
    ) { records, tracks ->
        val tracksById = tracks.associateBy { it.id }
        records.map { record ->
            Playlist(
                id = record.playlist.id,
                name = record.playlist.name,
                tracks = record.tracks.sortedBy { it.position }.mapNotNull { tracksById[it.trackId] },
            )
        }
    }

    override suspend fun create(name: String): Long {
        val normalized = validatedName(name)
        require(dao.countByName(normalized) == 0) { "Ya existe una playlist con ese nombre" }
        val timestamp = now()
        return dao.insertPlaylist(PlaylistEntity(name = normalized, createdAtEpochMs = timestamp, updatedAtEpochMs = timestamp))
    }

    override suspend fun rename(playlistId: Long, name: String) {
        val normalized = validatedName(name)
        require(dao.countByName(normalized, playlistId) == 0) { "Ya existe una playlist con ese nombre" }
        require(dao.rename(playlistId, normalized, now()) == 1) { DynamicMessages.playlistMissing }
    }

    override suspend fun delete(playlistId: Long) {
        dao.delete(playlistId)
    }

    override suspend fun addTrack(playlistId: Long, trackId: Long): Boolean = dao.addTrack(playlistId, trackId, now())

    override suspend fun removeTrack(playlistId: Long, trackId: Long): Boolean = dao.removeTrack(playlistId, trackId, now())

    override suspend fun moveTrack(playlistId: Long, fromIndex: Int, toIndex: Int) = dao.moveTrack(playlistId, fromIndex, toIndex, now())

    override suspend fun removeMissingTracks(validTrackIds: Set<Long>) {
        dao.cleanupMissingTracks(validTrackIds)
    }

    private fun validatedName(name: String): String {
        val normalized = name.trim()
        require(normalized.isNotEmpty()) { DynamicMessages.playlistNameRequired }
        require(normalized.length <= 80) { DynamicMessages.playlistNameTooLong }
        return normalized
    }
}
