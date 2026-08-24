package com.nightcallaudio.domain.model

data class Track(
    val id: Long,
    val contentUri: String,
    val title: String,
    val artist: String,
    val artistId: Long?,
    val album: String,
    val albumId: Long?,
    val artworkUri: String?,
    val durationMs: Long,
    val trackNumber: Int?,
    val discNumber: Int?,
    val genre: String?,
    val folder: String?,
    val year: Int?,
    val dateAddedEpochSeconds: Long?,
)
