package com.nightcallaudio.domain.usecase

import com.nightcallaudio.domain.model.Album
import com.nightcallaudio.domain.model.Artist
import com.nightcallaudio.domain.model.Genre
import com.nightcallaudio.domain.model.MusicFolder
import com.nightcallaudio.domain.model.MusicLibrary
import com.nightcallaudio.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetMusicLibraryUseCase(
    private val repository: MusicRepository,
) {
    suspend operator fun invoke(): MusicLibrary = buildLibrary(repository.getTracks())

    fun observe(): Flow<MusicLibrary> = repository.observeTracks().map(::buildLibrary)

    private fun buildLibrary(sourceTracks: List<com.nightcallaudio.domain.model.Track>): MusicLibrary {
        val tracks = sourceTracks.sortedBy { it.title.lowercase() }
        return MusicLibrary(
            tracks = tracks,
            artists = tracks.groupBy { it.artistId to it.artist }.map { (key, values) ->
                Artist(key.first, key.second, values.size)
            }.sortedBy { it.name.lowercase() },
            albums = tracks.groupBy { Triple(it.albumId, it.album, it.artist) }.map { (key, values) ->
                Album(key.first, key.second, key.third, values.size)
            }.sortedBy { it.title.lowercase() },
            genres = tracks.mapNotNull { it.genre }.groupingBy { it }.eachCount().map { (name, count) ->
                Genre(name, count)
            }.sortedBy { it.name.lowercase() },
            folders = tracks.mapNotNull { it.folder }.groupingBy { it }.eachCount().map { (name, count) ->
                MusicFolder(name, count)
            }.sortedBy { it.name.lowercase() },
        )
    }
}
