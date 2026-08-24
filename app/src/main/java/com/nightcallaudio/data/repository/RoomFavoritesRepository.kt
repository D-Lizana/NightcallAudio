package com.nightcallaudio.data.repository

import com.nightcallaudio.data.database.dao.FavoritesDao
import com.nightcallaudio.data.database.entity.FavoriteEntity
import com.nightcallaudio.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFavoritesRepository(
    private val dao: FavoritesDao,
    private val now: () -> Long = System::currentTimeMillis,
) : FavoritesRepository {
    override fun observeFavoriteIds(): Flow<Set<Long>> = dao.observeIds().map { it.toSet() }

    override suspend fun setFavorite(trackId: Long, favorite: Boolean) {
        if (favorite) dao.insert(FavoriteEntity(trackId, now())) else dao.delete(trackId)
    }

    override suspend fun removeMissing(validTrackIds: Set<Long>) {
        if (validTrackIds.isEmpty()) dao.clear() else dao.deleteMissing(validTrackIds)
    }
}
