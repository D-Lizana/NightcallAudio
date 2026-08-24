package com.nightcallaudio.domain.model

enum class RepeatMode {
    OFF,
    ALL,
    ONE,
}

enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    READY,
    ENDED,
}

data class PlaybackState(
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val isPlaying: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val errorMessage: String? = null,
) {
    val currentTrack: Track?
        get() = queue.getOrNull(currentIndex)
}
