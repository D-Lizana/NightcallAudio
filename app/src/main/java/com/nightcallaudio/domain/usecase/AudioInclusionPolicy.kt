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
        "whatsapp",
        "whatsapp audio",
        "whatsapp voice notes",
        "whatsapp business",
        "com.whatsapp",
        "com.whatsapp.w4b",
    )

    private val excludedAlbums = setOf(
        "whatsapp audio",
        "whatsapp voice notes",
    )

    fun shouldInclude(durationMs: Long, location: String?, album: String? = null): Boolean {
        if (durationMs < MINIMUM_DURATION_MS) return false
        val normalizedAlbum = album?.trim()?.lowercase(Locale.ROOT)
        if (normalizedAlbum in excludedAlbums) return false
        val normalized = location?.lowercase(Locale.ROOT) ?: return true
        return normalized.split('/', '\\').map(String::trim).none(excludedFolders::contains)
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
