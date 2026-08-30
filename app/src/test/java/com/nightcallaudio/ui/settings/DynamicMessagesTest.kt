package com.nightcallaudio.ui.settings

import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DynamicMessagesTest {
    private lateinit var original: Locale
    @Before fun rememberLocale() { original = Locale.getDefault() }
    @After fun restoreLocale() { Locale.setDefault(original) }

    @Test fun `uses Spanish messages by default locale`() {
        Locale.setDefault(Locale.forLanguageTag("es"))
        assertEquals("No se pudo completar la operación.", DynamicMessages.operationFailed)
    }

    @Test fun `uses English messages when selected`() {
        Locale.setDefault(Locale.forLanguageTag("en"))
        assertEquals("The operation could not be completed.", DynamicMessages.operationFailed)
    }
}
