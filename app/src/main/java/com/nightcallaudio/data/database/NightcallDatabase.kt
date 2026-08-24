package com.nightcallaudio.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nightcallaudio.data.database.dao.*
import com.nightcallaudio.data.database.entity.*

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        FavoriteEntity::class,
        QueueItemEntity::class,
        PlaybackStateEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class NightcallDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun playbackPersistenceDao(): PlaybackPersistenceDao

    companion object {
        const val DATABASE_NAME = "nightcall_audio.db"

        fun create(context: Context): NightcallDatabase = Room.databaseBuilder(
            context.applicationContext,
            NightcallDatabase::class.java,
            DATABASE_NAME,
        ).addMigrations(*DatabaseMigrations.ALL).build()
    }
}
