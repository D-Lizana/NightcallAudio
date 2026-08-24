package com.nightcallaudio

import androidx.annotation.OptIn
import androidx.media3.session.CommandButton
import androidx.media3.common.util.UnstableApi
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nightcallaudio.playback.NotificationCommands
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(markerClass = [UnstableApi::class])
class NotificationCommandsTest {
    @Test
    fun accionDetenerUtilizaOrdenDeSesionPropia() {
        val button = NotificationCommands.stopButton("Detener")

        assertEquals(CommandButton.ICON_STOP, button.icon)
        assertEquals("Detener", button.displayName)
        assertEquals(NotificationCommands.STOP_ACTION, button.sessionCommand?.customAction)
        assertTrue(button.slots.contains(CommandButton.SLOT_OVERFLOW))
    }
}
