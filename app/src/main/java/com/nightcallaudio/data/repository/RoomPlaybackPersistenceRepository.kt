package com.nightcallaudio.data.repository

import androidx.room.withTransaction
import com.nightcallaudio.data.database.NightcallDatabase
import com.nightcallaudio.data.database.entity.PlaybackStateEntity
import com.nightcallaudio.domain.model.RepeatMode
import com.nightcallaudio.domain.repository.PersistedPlaybackSession
import com.nightcallaudio.domain.repository.PlaybackPersistenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomPlaybackPersistenceRepository(
    private val database: NightcallDatabase,
    private val now: () -> Long = System::currentTimeMillis,
) : PlaybackPersistenceRepository {
    private val dao = database.playbackPersistenceDao()

    override fun observe(): Flow<PersistedPlaybackSession?> = combine(
        dao.observeQueue(),
        dao.observeState(),
    ) { queue, state ->
        state?.let {
            PersistedPlaybackSession(
                trackIds = queue.map { item -> item.trackId },
                originalPositions = queue.map { item -> item.originalPosition },
                currentIndex = it.currentIndex,
                positionMs = it.positionMs,
                shuffleEnabled = it.shuffleEnabled,
                repeatMode = runCatching { RepeatMode.valueOf(it.repeatMode) }.getOrDefault(RepeatMode.OFF),
            )
        }
    }

    override suspend fun save(session: PersistedPlaybackSession) {
        database.withTransaction {
            dao.replaceQueue(session.trackIds, session.originalPositions)
            dao.upsertState(
                PlaybackStateEntity(
                    currentIndex = session.currentIndex,
                    positionMs = session.positionMs.coerceAtLeast(0),
                    shuffleEnabled = session.shuffleEnabled,
                    repeatMode = session.repeatMode.name,
                    updatedAtEpochMs = now(),
                ),
            )
        }
    }
}
