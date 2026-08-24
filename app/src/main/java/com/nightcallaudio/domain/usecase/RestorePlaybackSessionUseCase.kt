package com.nightcallaudio.domain.usecase

import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.repository.PersistedPlaybackSession

data class ResolvedPlaybackSession(
    val tracks: List<Track>,
    val originalPositions: List<Int>,
    val currentIndex: Int,
    val positionMs: Long,
)

class RestorePlaybackSessionUseCase {
    operator fun invoke(
        session: PersistedPlaybackSession,
        availableTracks: List<Track>,
    ): ResolvedPlaybackSession? {
        val tracksById = availableTracks.associateBy(Track::id)
        val resolved = session.trackIds.mapIndexedNotNull { oldIndex, id ->
            tracksById[id]?.let { track ->
                ResolvedEntry(
                    oldIndex = oldIndex,
                    track = track,
                    originalPosition = session.originalPositions.getOrElse(oldIndex) { oldIndex },
                )
            }
        }
        if (resolved.isEmpty()) return null

        val currentIndex = resolved.indexOfFirst { it.oldIndex == session.currentIndex }
            .takeIf { it >= 0 }
            ?: resolved.indexOfFirst { it.oldIndex > session.currentIndex }
                .takeIf { it >= 0 }
            ?: resolved.lastIndex

        return ResolvedPlaybackSession(
            tracks = resolved.map(ResolvedEntry::track),
            originalPositions = resolved.map(ResolvedEntry::originalPosition),
            currentIndex = currentIndex,
            positionMs = if (resolved[currentIndex].oldIndex == session.currentIndex) {
                session.positionMs.coerceAtLeast(0L)
            } else {
                0L
            },
        )
    }

    private data class ResolvedEntry(
        val oldIndex: Int,
        val track: Track,
        val originalPosition: Int,
    )
}
