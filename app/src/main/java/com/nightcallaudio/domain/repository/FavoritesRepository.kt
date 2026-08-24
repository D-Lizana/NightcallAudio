package com.nightcallaudio.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoriteIds(): Flow<Set<Long>>
    suspend fun setFavorite(trackId: Long, favorite: Boolean)
}
