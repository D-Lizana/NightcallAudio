package com.nightcallaudio

import com.nightcallaudio.domain.usecase.AudioInclusionPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AudioInclusionPolicyTest {
    @Test
    fun `excluye audios inferiores a treinta segundos`() {
        assertFalse(AudioInclusionPolicy.shouldInclude(29_999, "Music/Nightcall"))
    }

    @Test
    fun `incluye audios de treinta segundos`() {
        assertTrue(AudioInclusionPolicy.shouldInclude(30_000, "Music/Nightcall"))
    }

    @Test
    fun `excluye carpetas de tonos y grabaciones sin distinguir mayusculas`() {
        assertFalse(AudioInclusionPolicy.shouldInclude(180_000, "Audio/RINGTONES"))
        assertFalse(AudioInclusionPolicy.shouldInclude(180_000, "Recordings/Entrevistas"))
        assertFalse(AudioInclusionPolicy.shouldInclude(180_000, "Audio/Grabaciones"))
    }

    @Test
    fun `no excluye una carpeta musical por una coincidencia parcial`() {
        assertTrue(AudioInclusionPolicy.shouldInclude(180_000, "Music/My Recordings Album"))
    }

    @Test
    fun `obtiene carpeta desde relative path moderno`() {
        assertEquals("Synthwave", AudioInclusionPolicy.folderName("Music/Synthwave/", true))
    }

    @Test
    fun `obtiene carpeta padre desde ruta de archivo en Android antiguo`() {
        assertEquals("Synthwave", AudioInclusionPolicy.folderName("/storage/emulated/0/Music/Synthwave/song.mp3", false))
        assertNull(AudioInclusionPolicy.folderName("song.mp3", false))
    }
}
