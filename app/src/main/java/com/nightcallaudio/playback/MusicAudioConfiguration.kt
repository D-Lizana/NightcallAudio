package com.nightcallaudio.playback

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C

/** Configuración única de salida y convivencia con otras aplicaciones de audio. */
object MusicAudioConfiguration {
    val audioAttributes: AudioAttributes
        get() = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

    const val HANDLE_AUDIO_FOCUS = true
    const val PAUSE_WHEN_AUDIO_BECOMES_NOISY = true
}
