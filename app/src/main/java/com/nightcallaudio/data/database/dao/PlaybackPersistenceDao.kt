package com.nightcallaudio.data.database.dao

import androidx.room.*
import com.nightcallaudio.data.database.entity.PlaybackStateEntity
import com.nightcallaudio.data.database.entity.QueueItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PlaybackPersistenceDao {
    @Query("SELECT * FROM persisted_queue ORDER BY position")
    abstract fun observeQueue(): Flow<List<QueueItemEntity>>

    @Query("SELECT * FROM playback_state WHERE singletonId = 0")
    abstract fun observeState(): Flow<PlaybackStateEntity?>

    @Query("DELETE FROM persisted_queue")
    abstract suspend fun clearQueue()

    @Insert
    abstract suspend fun insertQueue(items: List<QueueItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertState(state: PlaybackStateEntity)

    @Transaction
    open suspend fun replaceQueue(trackIds: List<Long>, originalPositions: List<Int>) {
        require(trackIds.size == originalPositions.size)
        clearQueue()
        insertQueue(trackIds.mapIndexed { index, id -> QueueItemEntity(index, id, originalPositions[index]) })
    }
}
