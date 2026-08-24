package com.nightcallaudio.domain.model

enum class RepeatMode {
    OFF,
    ALL,
    ONE,
}

data class PlaybackState(
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val isPlaying: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
) {
    val currentTrack: Track?
        get() = queue.getOrNull(currentIndex)
}
