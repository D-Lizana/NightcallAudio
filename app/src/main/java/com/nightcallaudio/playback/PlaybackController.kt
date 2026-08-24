package com.nightcallaudio.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.*
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.nightcallaudio.domain.model.PlaybackState
import com.nightcallaudio.domain.model.PlaybackStatus
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.repository.PlaybackRepository
import com.nightcallaudio.domain.usecase.PreviousAction
import com.nightcallaudio.domain.usecase.PreviousButtonPolicy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackController(context: Context) : PlaybackRepository {
    private val applicationContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressJob: Job? = null
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
                    updateProgressLoop(player)
                }

                override fun onPlayerError(error: PlaybackException) {
                    _state.value = _state.value.copy(
                        errorMessage = "No se pudo reproducir la canción. Se intentará continuar con la cola.",
                    )
                }
            })
            publishPlayerState(controller)
        }
    }

    override fun play(tracks: List<Track>, selectedIndex: Int) {
        require(selectedIndex in tracks.indices) { "El índice seleccionado no pertenece a la cola" }
        _state.value = PlaybackState(queue = tracks, currentIndex = selectedIndex, status = PlaybackStatus.BUFFERING)
        withController { controller ->
            controller.setMediaItems(tracks.map { it.toMediaItem() }, selectedIndex, 0L)
            controller.prepare()
            controller.play()
        }
    }

    override fun play() = withController(MediaController::play)

    override fun pause() = withController(MediaController::pause)

    override fun seekTo(positionMs: Long) = withController { controller ->
        val upperBound = controller.duration.takeIf { it != C.TIME_UNSET && it >= 0 } ?: Long.MAX_VALUE
        controller.seekTo(positionMs.coerceIn(0L, upperBound))
    }

    override fun seekBack() = withController(MediaController::seekBack)

    override fun seekForward() = withController(MediaController::seekForward)

    override fun skipToNext() = withController { controller ->
        if (controller.hasNextMediaItem()) controller.seekToNextMediaItem()
    }

    override fun skipToPrevious() = withController { controller ->
        when (PreviousButtonPolicy.action(controller.currentPosition)) {
            PreviousAction.RESTART_CURRENT -> controller.seekTo(0L)
            PreviousAction.PLAY_PREVIOUS -> {
                if (controller.hasPreviousMediaItem()) controller.seekToPreviousMediaItem() else controller.seekTo(0L)
            }
        }
    }

    override fun stop() = withController { controller ->
        controller.stop()
        controller.clearMediaItems()
        _state.value = PlaybackState()
    }

    override fun close() {
        progressJob?.cancel()
        scope.cancel()
        MediaController.releaseFuture(controllerFuture)
    }

    private fun withController(action: (MediaController) -> Unit) {
        controllerFuture.addListener(
            {
                runCatching { action(controllerFuture.get()) }.onFailure { error ->
                    _state.value = _state.value.copy(errorMessage = error.message ?: "No se pudo conectar con el reproductor.")
                }
            },
            ContextCompat.getMainExecutor(applicationContext),
        )
    }

    private fun updateProgressLoop(player: Player) {
        progressJob?.cancel()
        if (!player.isPlaying) return
        progressJob = scope.launch {
            while (isActive && player.isPlaying) {
                publishPlayerState(player)
                delay(PROGRESS_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun publishPlayerState(player: Player) {
        val duration = player.duration.takeIf { it != C.TIME_UNSET && it >= 0 } ?: 0L
        _state.value = _state.value.copy(
            currentIndex = player.currentMediaItemIndex.takeIf { it != C.INDEX_UNSET } ?: -1,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = duration,
            isPlaying = player.isPlaying,
            shuffleEnabled = player.shuffleModeEnabled,
            status = player.playbackState.toDomainStatus(),
            errorMessage = player.playerError?.message,
        )
    }

    private fun Int.toDomainStatus(): PlaybackStatus = when (this) {
        Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
        Player.STATE_READY -> PlaybackStatus.READY
        Player.STATE_ENDED -> PlaybackStatus.ENDED
        else -> PlaybackStatus.IDLE
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
                .setTrackNumber(trackNumber)
                .setDiscNumber(discNumber)
                .setIsPlayable(true)
                .build(),
        )
        .build()

    private companion object {
        const val PROGRESS_UPDATE_INTERVAL_MS = 500L
    }
}
