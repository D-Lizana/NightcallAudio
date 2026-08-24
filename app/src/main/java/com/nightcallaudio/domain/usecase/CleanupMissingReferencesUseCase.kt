package com.nightcallaudio.domain.usecase

import com.nightcallaudio.domain.repository.FavoritesRepository
import com.nightcallaudio.domain.repository.PlaylistRepository

class CleanupMissingReferencesUseCase(
    private val playlists: PlaylistRepository,
    private val favorites: FavoritesRepository,
) {
    suspend operator fun invoke(validTrackIds: Set<Long>) {
        playlists.removeMissingTracks(validTrackIds)
        favorites.removeMissing(validTrackIds)
    }
}
