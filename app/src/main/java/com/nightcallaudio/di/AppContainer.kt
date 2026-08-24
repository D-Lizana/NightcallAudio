package com.nightcallaudio.di

import android.content.Context
import com.nightcallaudio.data.mediastore.MediaStoreMusicRepository
import com.nightcallaudio.domain.repository.MusicRepository
import com.nightcallaudio.domain.repository.PlaybackRepository
import com.nightcallaudio.domain.usecase.GetMusicLibraryUseCase
import com.nightcallaudio.domain.usecase.SearchTracksUseCase
import com.nightcallaudio.playback.PlaybackController

class AppContainer(context: Context) {
    val musicRepository: MusicRepository = MediaStoreMusicRepository(context)
    val playbackRepository: PlaybackRepository = PlaybackController(context)
    val getMusicLibrary = GetMusicLibraryUseCase(musicRepository)
    val searchTracks = SearchTracksUseCase()
}
