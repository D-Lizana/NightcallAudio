package com.nightcallaudio.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.nightcallaudio.domain.model.Track

class PlaybackController(context: Context) : AutoCloseable {
    private val applicationContext = context.applicationContext
    private val controllerFuture: ListenableFuture<MediaController> = MediaController.Builder(
        applicationContext,
        SessionToken(applicationContext, ComponentName(applicationContext, PlaybackService::class.java)),
    ).buildAsync()

    fun play(tracks: List<Track>, selectedIndex: Int) {
        controllerFuture.addListener(
            {
                val controller = controllerFuture.get()
                controller.setMediaItems(tracks.map { it.toMediaItem() }, selectedIndex, 0L)
                controller.prepare()
                controller.play()
            },
            ContextCompat.getMainExecutor(applicationContext),
        )
    }

    override fun close() {
        MediaController.releaseFuture(controllerFuture)
    }

    private fun Track.toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setIsPlayable(true)
                .build(),
        )
        .build()
}
