package com.nightcallaudio

import com.nightcallaudio.domain.usecase.*
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackPoliciesTest {
    @Test
    fun `anterior reinicia solo despues de tres segundos`() {
        assertEquals(PreviousAction.PLAY_PREVIOUS, PreviousButtonPolicy.action(3_000))
        assertEquals(PreviousAction.RESTART_CURRENT, PreviousButtonPolicy.action(3_001))
    }

    @Test
    fun `un fallo salta si queda otra pista y se detiene al final`() {
        assertEquals(PlaybackFailureAction.SKIP_TO_NEXT, PlaybackFailurePolicy.action(true))
        assertEquals(PlaybackFailureAction.STOP, PlaybackFailurePolicy.action(false))
    }
}
