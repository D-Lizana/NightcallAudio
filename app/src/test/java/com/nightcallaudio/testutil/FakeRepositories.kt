package com.nightcallaudio.testutil

import com.nightcallaudio.domain.model.PlaybackState
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.repository.MusicRepository
import com.nightcallaudio.domain.repository.PlaybackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMusicRepository(
    var tracks: List<Track> = emptyList(),
) : MusicRepository {
    override suspend fun getTracks(): List<Track> = tracks
    override fun observeTracks(): Flow<List<Track>> = flowOf(tracks)
}

class FakePlaybackRepository : PlaybackRepository {
    private val mutableState = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = mutableState

    override fun play(tracks: List<Track>, selectedIndex: Int) {
        mutableState.value = PlaybackState(queue = tracks, currentIndex = selectedIndex, isPlaying = true)
    }

    override fun play() {
        mutableState.value = mutableState.value.copy(isPlaying = true)
    }

    override fun pause() {
        mutableState.value = mutableState.value.copy(isPlaying = false)
    }

    override fun seekTo(positionMs: Long) {
        mutableState.value = mutableState.value.copy(positionMs = positionMs)
    }

    override fun skipToNext() {
        val next = (mutableState.value.currentIndex + 1).coerceAtMost(mutableState.value.queue.lastIndex)
        mutableState.value = mutableState.value.copy(currentIndex = next)
    }

    override fun skipToPrevious() {
        val previous = (mutableState.value.currentIndex - 1).coerceAtLeast(0)
        mutableState.value = mutableState.value.copy(currentIndex = previous)
    }

    override fun close() = Unit
}
