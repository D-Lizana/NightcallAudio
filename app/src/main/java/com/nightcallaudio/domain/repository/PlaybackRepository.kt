package com.nightcallaudio.domain.repository

import com.nightcallaudio.domain.model.PlaybackState
import com.nightcallaudio.domain.model.Track
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
    fun stop()
}
