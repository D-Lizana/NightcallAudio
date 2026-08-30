package com.nightcallaudio.ui.settings

import java.util.Locale

object DynamicMessages {
    private fun text(es: String, en: String) = if (Locale.getDefault().language == "en") en else es
    val libraryReadFailed get() = text("No se ha podido leer la música del dispositivo.", "Music on this device could not be read.")
    val duplicatePlaylistTrack get() = text("La canción ya está en esa playlist.", "The song is already in that playlist.")
    val operationFailed get() = text("No se pudo completar la operación.", "The operation could not be completed.")
    val playbackFailed get() = text("No se pudo reproducir la canción. Se intentará continuar con la cola.", "The song could not be played. Nightcall will try to continue with the queue.")
    val playerConnectionFailed get() = text("No se pudo conectar con el reproductor.", "Could not connect to the player.")
    val playerConnectionLost get() = text("Se ha perdido la conexión con el reproductor. Reconectando…", "Connection to the player was lost. Reconnecting…")
    val invalidQueueIndex get() = text("El índice seleccionado no pertenece a la cola", "The selected index is not in the queue")
    val playlistMissing get() = text("La playlist no existe", "The playlist does not exist")
    val playlistNameRequired get() = text("El nombre no puede estar vacío", "The name cannot be empty")
    val playlistNameTooLong get() = text("El nombre no puede superar 80 caracteres", "The name cannot exceed 80 characters")
}
