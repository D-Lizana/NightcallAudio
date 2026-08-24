package com.nightcallaudio.domain.usecase

import java.util.Locale

object AudioInclusionPolicy {
    const val MINIMUM_DURATION_MS = 30_000L

    private val excludedFolders = setOf(
        "alarms",
        "notifications",
        "ringtones",
        "recordings",
        "recorders",
        "voice recorder",
    )

    fun shouldInclude(durationMs: Long, location: String?): Boolean {
        if (durationMs < MINIMUM_DURATION_MS) return false
        val normalized = location?.lowercase(Locale.ROOT) ?: return true
        return normalized.split('/', '\\').none(excludedFolders::contains)
    }
}
