package com.nightcallaudio.domain.usecase

import java.util.Locale

object AudioInclusionPolicy {
    const val MINIMUM_DURATION_MS = 30_000L

    private val excludedFolders = setOf(
        "alarms",
        "alarmas",
        "notifications",
        "notificaciones",
        "ringtones",
        "tonos",
        "tonos de llamada",
        "recordings",
        "recorders",
        "grabaciones",
        "voice recorder",
        "voice recordings",
        "call recordings",
    )

    fun shouldInclude(durationMs: Long, location: String?): Boolean {
        if (durationMs < MINIMUM_DURATION_MS) return false
        val normalized = location?.lowercase(Locale.ROOT) ?: return true
        return normalized.split('/', '\\').none(excludedFolders::contains)
    }

    fun folderName(location: String?, isRelativePath: Boolean): String? {
        val value = location?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val normalized = value.replace('\\', '/').trimEnd('/')
        return if (isRelativePath) {
            normalized.substringAfterLast('/').takeIf(String::isNotBlank)
        } else {
            normalized.substringBeforeLast('/', missingDelimiterValue = "")
                .substringAfterLast('/')
                .takeIf(String::isNotBlank)
        }
    }
}
