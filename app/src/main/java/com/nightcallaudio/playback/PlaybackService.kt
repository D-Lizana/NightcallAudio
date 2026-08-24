package com.nightcallaudio.playback

import android.content.Intent
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.nightcallaudio.domain.usecase.PlaybackFailureAction
import com.nightcallaudio.domain.usecase.PlaybackFailurePolicy

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val player: Player?
        get() = mediaSession?.player

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(SEEK_INCREMENT_MS)
            .setSeekForwardIncrementMs(SEEK_INCREMENT_MS)
            .build().apply {
            setAudioAttributes(
                MusicAudioConfiguration.audioAttributes,
                MusicAudioConfiguration.HANDLE_AUDIO_FOCUS,
            )
            setHandleAudioBecomingNoisy(MusicAudioConfiguration.PAUSE_WHEN_AUDIO_BECOMES_NOISY)
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    when (PlaybackFailurePolicy.action(hasNextMediaItem())) {
                        PlaybackFailureAction.SKIP_TO_NEXT -> {
                            seekToNextMediaItem()
                            prepare()
                            play()
                        }
                        PlaybackFailureAction.STOP -> stop()
                    }
                }
            })
        }
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val activePlayer = player
        if (
            activePlayer == null ||
            !activePlayer.playWhenReady ||
            activePlayer.mediaItemCount == 0 ||
            activePlayer.playbackState == Player.STATE_ENDED
        ) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private companion object {
        const val SEEK_INCREMENT_MS = 10_000L
    }
}
