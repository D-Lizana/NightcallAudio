package com.nightcallaudio.playback

import androidx.annotation.OptIn
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand
import androidx.media3.common.util.UnstableApi

@OptIn(markerClass = [UnstableApi::class])
object NotificationCommands {
    const val STOP_ACTION = "com.nightcallaudio.action.STOP_SESSION"

    val stopSessionCommand: SessionCommand
        get() = SessionCommand(STOP_ACTION, android.os.Bundle.EMPTY)

    fun stopButton(label: String): CommandButton = CommandButton.Builder(CommandButton.ICON_STOP)
        .setSessionCommand(stopSessionCommand)
        .setDisplayName(label)
        .setSlots(CommandButton.SLOT_OVERFLOW)
        .build()
}
