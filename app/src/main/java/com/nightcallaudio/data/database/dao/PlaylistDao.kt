package com.nightcallaudio.data.database.dao

import androidx.room.*
import com.nightcallaudio.data.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PlaylistDao {
    @Transaction
    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE")
    abstract fun observeAll(): Flow<List<PlaylistWithTracks>>

    @Insert
    abstract suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name, updatedAtEpochMs = :updatedAt WHERE id = :playlistId")
    abstract suspend fun rename(playlistId: Long, name: String, updatedAt: Long): Int

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    abstract suspend fun delete(playlistId: Long): Int

    @Query("SELECT COUNT(*) FROM playlists WHERE name = :name COLLATE NOCASE AND id != :excludedId")
    abstract suspend fun countByName(name: String, excludedId: Long = -1): Int

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_tracks WHERE playlistId = :playlistId")
    abstract suspend fun maxPosition(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertTrack(item: PlaylistTrackEntity): Long

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    abstract suspend fun deleteTrack(playlistId: Long, trackId: Long): Int

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY position")
    abstract suspend fun tracksOnce(playlistId: Long): List<PlaylistTrackEntity>

    @Update
    abstract suspend fun updateTrack(item: PlaylistTrackEntity)

    @Transaction
    open suspend fun addTrack(playlistId: Long, trackId: Long, now: Long): Boolean {
        val position = maxPosition(playlistId) + 1
        val inserted = insertTrack(PlaylistTrackEntity(playlistId, trackId, position, now)) != -1L
        if (inserted) touch(playlistId, now)
        return inserted
    }

    @Transaction
    open suspend fun removeTrack(playlistId: Long, trackId: Long, now: Long): Boolean {
        if (deleteTrack(playlistId, trackId) == 0) return false
        normalizePositions(playlistId)
        touch(playlistId, now)
        return true
    }

    @Transaction
    open suspend fun moveTrack(playlistId: Long, fromIndex: Int, toIndex: Int, now: Long) {
        val current = tracksOnce(playlistId)
        require(fromIndex in current.indices && toIndex in current.indices)
        if (fromIndex == toIndex) return
        val reordered = current.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        current.forEachIndexed { index, item -> updateTrack(item.copy(position = -index - 1)) }
        reordered.forEachIndexed { index, item -> updateTrack(item.copy(position = index)) }
        touch(playlistId, now)
    }

    private suspend fun normalizePositions(playlistId: Long) {
        tracksOnce(playlistId).forEachIndexed { index, item ->
            if (item.position != index) updateTrack(item.copy(position = index))
        }
    }

    @Query("UPDATE playlists SET updatedAtEpochMs = :updatedAt WHERE id = :playlistId")
    abstract suspend fun touch(playlistId: Long, updatedAt: Long)

    @Query("DELETE FROM playlist_tracks WHERE trackId NOT IN (:validTrackIds)")
    protected abstract suspend fun deleteMissingTrackRows(validTrackIds: Set<Long>)

    @Query("DELETE FROM playlist_tracks")
    protected abstract suspend fun clearAllTracks()

    @Query("SELECT id FROM playlists")
    protected abstract suspend fun playlistIds(): List<Long>

    @Transaction
    open suspend fun cleanupMissingTracks(validTrackIds: Set<Long>) {
        if (validTrackIds.isEmpty()) clearAllTracks() else deleteMissingTrackRows(validTrackIds)
        playlistIds().forEach { normalizePositions(it) }
    }
}
