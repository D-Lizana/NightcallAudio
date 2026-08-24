package com.nightcallaudio.domain.repository

import com.nightcallaudio.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getTracks(): List<Track>
    fun observeTracks(): Flow<List<Track>>
}
