package com.nightcallaudio.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.model.PlaybackState
import com.nightcallaudio.domain.repository.PlaybackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackController(context: Context) : PlaybackRepository {
    private val applicationContext = context.applicationContext
    private val controllerFuture: ListenableFuture<MediaController> = MediaController.Builder(
        applicationContext,
        SessionToken(applicationContext, ComponentName(applicationContext, PlaybackService::class.java)),
    ).buildAsync()
    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    init {
        withController { controller ->
            controller.addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    publishPlayerState(player)
                }
            })
            publishPlayerState(controller)
        }
    }

    override fun play(tracks: List<Track>, selectedIndex: Int) {
        require(selectedIndex in tracks.indices) { "El índice seleccionado no pertenece a la cola" }
        _state.value = _state.value.copy(queue = tracks, currentIndex = selectedIndex)
        withController { controller ->
            controller.setMediaItems(tracks.map { it.toMediaItem() }, selectedIndex, 0L)
            controller.prepare()
            controller.play()
        }
    }

    override fun play() = withController(MediaController::play)

    override fun pause() = withController(MediaController::pause)

    override fun seekTo(positionMs: Long) = withController { it.seekTo(positionMs.coerceAtLeast(0L)) }

    override fun skipToNext() = withController(MediaController::seekToNextMediaItem)

    override fun skipToPrevious() = withController(MediaController::seekToPreviousMediaItem)

    override fun close() {
        MediaController.releaseFuture(controllerFuture)
    }

    private fun withController(action: (MediaController) -> Unit) {
        controllerFuture.addListener(
            { action(controllerFuture.get()) },
            ContextCompat.getMainExecutor(applicationContext),
        )
    }

    private fun publishPlayerState(player: Player) {
        _state.value = _state.value.copy(
            currentIndex = player.currentMediaItemIndex,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.coerceAtLeast(0L),
            isPlaying = player.isPlaying,
            shuffleEnabled = player.shuffleModeEnabled,
        )
    }

    private fun Track.toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(contentUri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(artworkUri?.let(Uri::parse))
                .setIsPlayable(true)
                .build(),
        )
        .build()
}
