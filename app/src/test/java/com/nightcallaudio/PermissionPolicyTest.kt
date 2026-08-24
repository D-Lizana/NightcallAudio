package com.nightcallaudio

import android.Manifest
import com.nightcallaudio.permissions.AudioPermissionState
import com.nightcallaudio.permissions.PermissionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionPolicyTest {
    @Test
    fun `usa permiso de almacenamiento hasta Android 12L`() {
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE, PermissionPolicy.audioPermission(32))
    }

    @Test
    fun `usa permiso especifico de audio desde Android 13`() {
        assertEquals(Manifest.permission.READ_MEDIA_AUDIO, PermissionPolicy.audioPermission(33))
    }

    @Test
    fun `distingue primera solicitud denegacion y denegacion permanente`() {
        assertEquals(AudioPermissionState.NOT_REQUESTED, PermissionPolicy.evaluateAudioPermission(false, false, false))
        assertEquals(AudioPermissionState.DENIED, PermissionPolicy.evaluateAudioPermission(false, true, true))
        assertEquals(AudioPermissionState.PERMANENTLY_DENIED, PermissionPolicy.evaluateAudioPermission(false, true, false))
        assertEquals(AudioPermissionState.GRANTED, PermissionPolicy.evaluateAudioPermission(true, true, false))
    }

    @Test
    fun `notificaciones requieren permiso solo desde Android 13`() {
        assertFalse(PermissionPolicy.requiresNotificationPermission(32))
        assertTrue(PermissionPolicy.requiresNotificationPermission(33))
    }
}
