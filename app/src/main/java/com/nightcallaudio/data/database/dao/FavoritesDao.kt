package com.nightcallaudio.data.database.dao

import androidx.room.*
import com.nightcallaudio.data.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT trackId FROM favorites ORDER BY createdAtEpochMs DESC")
    fun observeIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE trackId = :trackId")
    suspend fun delete(trackId: Long)

    @Query("DELETE FROM favorites WHERE trackId NOT IN (:validTrackIds)")
    suspend fun deleteMissing(validTrackIds: Set<Long>)

    @Query("DELETE FROM favorites")
    suspend fun clear()
}
