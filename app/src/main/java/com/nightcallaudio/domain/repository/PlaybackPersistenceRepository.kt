package com.nightcallaudio.domain.repository

import com.nightcallaudio.domain.model.RepeatMode
import kotlinx.coroutines.flow.Flow

data class PersistedPlaybackSession(
    val trackIds: List<Long>,
    val originalPositions: List<Int>,
    val currentIndex: Int,
    val positionMs: Long,
    val shuffleEnabled: Boolean,
    val repeatMode: RepeatMode,
)

interface PlaybackPersistenceRepository {
    fun observe(): Flow<PersistedPlaybackSession?>
    suspend fun save(session: PersistedPlaybackSession)
}
