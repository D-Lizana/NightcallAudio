package com.nightcallaudio.testutil

import com.nightcallaudio.domain.model.PlaybackState
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.model.RepeatMode
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

    override fun seekBack() = seekTo((mutableState.value.positionMs - 10_000).coerceAtLeast(0))

    override fun seekForward() = seekTo(mutableState.value.positionMs + 10_000)

    override fun skipToNext() {
        val next = (mutableState.value.currentIndex + 1).coerceAtMost(mutableState.value.queue.lastIndex)
        mutableState.value = mutableState.value.copy(currentIndex = next)
    }

    override fun skipToPrevious() {
        val previous = (mutableState.value.currentIndex - 1).coerceAtLeast(0)
        mutableState.value = mutableState.value.copy(currentIndex = previous)
    }

    override fun skipTo(index: Int) {
        if (index in mutableState.value.queue.indices) mutableState.value = mutableState.value.copy(currentIndex = index)
    }

    override fun playNext(track: Track) {
        val state = mutableState.value
        val queue = state.queue.toMutableList().apply { add((state.currentIndex + 1).coerceIn(0, size), track) }
        mutableState.value = state.copy(queue = queue)
    }

    override fun addToQueue(track: Track) {
        mutableState.value = mutableState.value.copy(queue = mutableState.value.queue + track)
    }

    override fun removeFromQueue(index: Int) {
        if (index in mutableState.value.queue.indices) {
            mutableState.value = mutableState.value.copy(queue = mutableState.value.queue.toMutableList().apply { removeAt(index) })
        }
    }

    override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val queue = mutableState.value.queue.toMutableList()
        if (fromIndex in queue.indices && toIndex in queue.indices) {
            queue.add(toIndex, queue.removeAt(fromIndex))
            mutableState.value = mutableState.value.copy(queue = queue)
        }
    }

    override fun setShuffleEnabled(enabled: Boolean) {
        mutableState.value = mutableState.value.copy(shuffleEnabled = enabled)
    }

    override fun setRepeatMode(mode: RepeatMode) {
        mutableState.value = mutableState.value.copy(repeatMode = mode)
    }

    override fun stop() {
        mutableState.value = PlaybackState()
    }

    override fun close() = Unit
}
