package com.nightcallaudio.permissions

import android.content.Context

class PermissionRequestStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "permission_requests",
        Context.MODE_PRIVATE,
    )

    var audioWasRequested: Boolean
        get() = preferences.getBoolean(KEY_AUDIO_REQUESTED, false)
        set(value) { preferences.edit().putBoolean(KEY_AUDIO_REQUESTED, value).apply() }

    var notificationsWereRequested: Boolean
        get() = preferences.getBoolean(KEY_NOTIFICATIONS_REQUESTED, false)
        set(value) { preferences.edit().putBoolean(KEY_NOTIFICATIONS_REQUESTED, value).apply() }

    private companion object {
        const val KEY_AUDIO_REQUESTED = "audio_requested"
        const val KEY_NOTIFICATIONS_REQUESTED = "notifications_requested"
    }
}
