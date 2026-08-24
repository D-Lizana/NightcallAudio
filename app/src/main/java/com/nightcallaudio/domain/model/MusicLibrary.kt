package com.nightcallaudio.domain.model

data class MusicLibrary(
    val tracks: List<Track>,
    val artists: List<Artist>,
    val albums: List<Album>,
    val genres: List<Genre>,
    val folders: List<MusicFolder>,
)

data class Artist(
    val id: Long?,
    val name: String,
    val trackCount: Int,
)

data class Album(
    val id: Long?,
    val title: String,
    val artist: String,
    val trackCount: Int,
)

data class Genre(
    val name: String,
    val trackCount: Int,
)

data class MusicFolder(
    val name: String,
    val trackCount: Int,
)
