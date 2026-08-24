package com.nightcallaudio.domain.usecase

import com.nightcallaudio.domain.model.Track

class SearchTracksUseCase {
    operator fun invoke(tracks: List<Track>, query: String): List<Track> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return tracks
        return tracks.filter { track ->
            track.title.contains(normalized, ignoreCase = true) ||
                track.artist.contains(normalized, ignoreCase = true) ||
                track.album.contains(normalized, ignoreCase = true)
        }
    }
}
