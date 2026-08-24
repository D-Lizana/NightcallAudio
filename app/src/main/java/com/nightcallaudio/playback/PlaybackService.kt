package com.nightcallaudio.playback

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.nightcallaudio.R
import com.nightcallaudio.domain.usecase.PlaybackFailureAction
import com.nightcallaudio.domain.usecase.PlaybackFailurePolicy

@OptIn(markerClass = [UnstableApi::class])
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val player: Player?
        get() = mediaSession?.player

    override fun onCreate() {
        super.onCreate()
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .setChannelName(R.string.playback_notification_channel)
            .build()
            .apply { setSmallIcon(R.drawable.ic_notification_music) }
        setMediaNotificationProvider(notificationProvider)

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
        val stopButton = NotificationCommands.stopButton(getString(R.string.stop_playback_session))
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(SessionCallback())
            .setMediaButtonPreferences(listOf(stopButton))
            .build()
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

    private inner class SessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult = MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailableSessionCommands(
                MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                    .buildUpon()
                    .add(NotificationCommands.stopSessionCommand)
                    .build(),
            )
            .build()

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction != NotificationCommands.STOP_ACTION) {
                return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
            }
            session.player.pause()
            session.player.stop()
            session.player.clearMediaItems()
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private companion object {
        const val SEEK_INCREMENT_MS = 10_000L
        const val NOTIFICATION_CHANNEL_ID = "nightcallaudio_playback"
    }
}
