package com.nightcallaudio

import androidx.media3.common.C
import com.nightcallaudio.playback.MusicAudioConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicAudioConfigurationTest {
    @Test
    fun `usa atributos de musica y delega audio focus en Media3`() {
        val attributes = MusicAudioConfiguration.audioAttributes

        assertEquals(C.USAGE_MEDIA, attributes.usage)
        assertEquals(C.AUDIO_CONTENT_TYPE_MUSIC, attributes.contentType)
        assertTrue(MusicAudioConfiguration.HANDLE_AUDIO_FOCUS)
    }

    @Test
    fun `pausa al desconectar una salida de audio`() {
        assertTrue(MusicAudioConfiguration.PAUSE_WHEN_AUDIO_BECOMES_NOISY)
    }
}
