package com.nightcallaudio.permissions

import android.Manifest
import android.os.Build

enum class AudioPermissionState {
    GRANTED,
    NOT_REQUESTED,
    DENIED,
    PERMANENTLY_DENIED,
}

object PermissionPolicy {
    fun audioPermission(sdkInt: Int = Build.VERSION.SDK_INT): String =
        if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun evaluateAudioPermission(
        granted: Boolean,
        wasRequested: Boolean,
        shouldShowRationale: Boolean,
    ): AudioPermissionState = when {
        granted -> AudioPermissionState.GRANTED
        !wasRequested -> AudioPermissionState.NOT_REQUESTED
        shouldShowRationale -> AudioPermissionState.DENIED
        else -> AudioPermissionState.PERMANENTLY_DENIED
    }

    fun requiresNotificationPermission(sdkInt: Int = Build.VERSION.SDK_INT): Boolean =
        sdkInt >= Build.VERSION_CODES.TIRAMISU
}
