package com.nightcallaudio

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.ui.components.MessageState
import com.nightcallaudio.ui.components.TrackRow
import com.nightcallaudio.ui.theme.NightcallAudioTheme
import com.nightcallaudio.permissions.AudioPermissionState
import com.nightcallaudio.ui.permissions.AudioPermissionScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VisualComponentsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun estadoVacioMuestraMensajeYEjecutaAccion() {
        var clicked = false
        composeRule.setContent {
            NightcallAudioTheme {
                MessageState("No hay música", "Añade canciones al dispositivo", action = "Reintentar") {
                    clicked = true
                }
            }
        }

        composeRule.onNodeWithText("No hay música").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").performClick()
        assertTrue(clicked)
    }

    @Test
    fun filaDeCancionMuestraMetadatosPrincipales() {
        composeRule.setContent {
            NightcallAudioTheme {
                TrackRow(
                    Track(
                        id = 1,
                        contentUri = "content://audio/1",
                        title = "Nightcall",
                        artist = "Kavinsky",
                        artistId = 1,
                        album = "OutRun",
                        albumId = 1,
                        artworkUri = "content://albumart/1",
                        durationMs = 257_000,
                        trackNumber = 1,
                        discNumber = 1,
                        genre = "Synthwave",
                        folder = "Music",
                        year = 2010,
                        dateAddedEpochSeconds = 1_700_000_000,
                    ),
                    onClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Nightcall").assertIsDisplayed()
        composeRule.onNodeWithText("Kavinsky · OutRun").assertIsDisplayed()
        composeRule.onNodeWithText("4:17").assertIsDisplayed()
    }

    @Test
    fun permisoPermanenteOfreceAbrirAjustes() {
        composeRule.setContent {
            NightcallAudioTheme {
                AudioPermissionScreen(
                    state = AudioPermissionState.PERMANENTLY_DENIED,
                    onRequestPermission = {},
                    onOpenSettings = {},
                )
            }
        }

        composeRule.onNodeWithText("Activa el acceso desde Ajustes").assertIsDisplayed()
        composeRule.onNodeWithText("Abrir Ajustes").assertIsDisplayed()
    }
}
