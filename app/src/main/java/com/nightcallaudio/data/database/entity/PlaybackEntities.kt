package com.nightcallaudio.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "persisted_queue", indices = [Index("trackId")])
data class QueueItemEntity(
    @PrimaryKey val position: Int,
    val trackId: Long,
    val originalPosition: Int,
)

@Entity(tableName = "playback_state")
data class PlaybackStateEntity(
    @PrimaryKey val singletonId: Int = SINGLETON_ID,
    val currentIndex: Int,
    val positionMs: Long,
    val shuffleEnabled: Boolean,
    val repeatMode: String,
    val updatedAtEpochMs: Long,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
