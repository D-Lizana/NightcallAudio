package com.nightcallaudio.domain.repository

import com.nightcallaudio.domain.model.Track

interface MusicRepository {
    suspend fun getTracks(): List<Track>
}
