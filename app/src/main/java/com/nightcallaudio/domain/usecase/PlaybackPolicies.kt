package com.nightcallaudio.domain.usecase

enum class PreviousAction {
    RESTART_CURRENT,
    PLAY_PREVIOUS,
}

object PreviousButtonPolicy {
    const val RESTART_THRESHOLD_MS = 3_000L

    fun action(positionMs: Long): PreviousAction =
        if (positionMs > RESTART_THRESHOLD_MS) PreviousAction.RESTART_CURRENT else PreviousAction.PLAY_PREVIOUS
}

enum class PlaybackFailureAction {
    SKIP_TO_NEXT,
    STOP,
}

object PlaybackFailurePolicy {
    fun action(hasNextTrack: Boolean): PlaybackFailureAction =
        if (hasNextTrack) PlaybackFailureAction.SKIP_TO_NEXT else PlaybackFailureAction.STOP
}
