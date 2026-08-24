package com.nightcallaudio.di

import android.content.Context
import com.nightcallaudio.data.mediastore.MediaStoreMusicRepository
import com.nightcallaudio.domain.repository.MusicRepository
import com.nightcallaudio.domain.repository.PlaybackRepository
import com.nightcallaudio.domain.usecase.GetMusicLibraryUseCase
import com.nightcallaudio.domain.usecase.SearchTracksUseCase
import com.nightcallaudio.playback.PlaybackController
import com.nightcallaudio.data.database.NightcallDatabase
import com.nightcallaudio.data.repository.RoomFavoritesRepository
import com.nightcallaudio.data.repository.RoomPlaybackPersistenceRepository
import com.nightcallaudio.data.repository.RoomPlaylistRepository
import com.nightcallaudio.domain.repository.FavoritesRepository
import com.nightcallaudio.domain.repository.PlaybackPersistenceRepository
import com.nightcallaudio.domain.repository.PlaylistRepository
import com.nightcallaudio.domain.usecase.CleanupMissingReferencesUseCase

class AppContainer(context: Context) {
    val database: NightcallDatabase = NightcallDatabase.create(context)
    val musicRepository: MusicRepository = MediaStoreMusicRepository(context)
    val playbackRepository: PlaybackRepository = PlaybackController(context)
    val playlistRepository: PlaylistRepository = RoomPlaylistRepository(database.playlistDao(), musicRepository)
    val favoritesRepository: FavoritesRepository = RoomFavoritesRepository(database.favoritesDao())
    val playbackPersistenceRepository: PlaybackPersistenceRepository = RoomPlaybackPersistenceRepository(database)
    val getMusicLibrary = GetMusicLibraryUseCase(musicRepository)
    val searchTracks = SearchTracksUseCase()
    val cleanupMissingReferences = CleanupMissingReferencesUseCase(playlistRepository, favoritesRepository)
}
