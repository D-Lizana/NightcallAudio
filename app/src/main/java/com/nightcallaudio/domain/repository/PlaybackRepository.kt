package com.nightcallaudio.domain.repository

import com.nightcallaudio.domain.model.PlaybackState
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.model.RepeatMode
import kotlinx.coroutines.flow.StateFlow

interface PlaybackRepository : AutoCloseable {
    val state: StateFlow<PlaybackState>

    fun play(tracks: List<Track>, selectedIndex: Int)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun seekBack()
    fun seekForward()
    fun skipToNext()
    fun skipToPrevious()
    fun skipTo(index: Int)
    fun playNext(track: Track)
    fun addToQueue(track: Track)
    fun removeFromQueue(index: Int)
    fun moveQueueItem(fromIndex: Int, toIndex: Int)
    fun setShuffleEnabled(enabled: Boolean)
    fun setRepeatMode(mode: RepeatMode)
    fun stop()
}
