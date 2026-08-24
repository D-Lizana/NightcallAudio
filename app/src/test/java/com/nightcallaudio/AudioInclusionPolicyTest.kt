package com.nightcallaudio

import com.nightcallaudio.domain.usecase.AudioInclusionPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    }

    @Test
    fun `no excluye una carpeta musical por una coincidencia parcial`() {
        assertTrue(AudioInclusionPolicy.shouldInclude(180_000, "Music/My Recordings Album"))
    }
}
