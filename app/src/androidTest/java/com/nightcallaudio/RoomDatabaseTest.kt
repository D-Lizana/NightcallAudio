package com.nightcallaudio

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nightcallaudio.data.database.NightcallDatabase
import com.nightcallaudio.data.database.entity.FavoriteEntity
import com.nightcallaudio.data.repository.RoomPlaybackPersistenceRepository
import com.nightcallaudio.domain.model.RepeatMode
import com.nightcallaudio.domain.repository.PersistedPlaybackSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {
    private lateinit var database: NightcallDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            NightcallDatabase::class.java,
        ).build()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun playlistEvitaDuplicadosYConservaOrden() = runBlocking {
        val dao = database.playlistDao()
        val playlistId = dao.insertPlaylist(
            com.nightcallaudio.data.database.entity.PlaylistEntity(name = "Viaje", createdAtEpochMs = 1, updatedAtEpochMs = 1),
        )

        Assert.assertTrue(dao.addTrack(playlistId, 10, 2))
        Assert.assertTrue(dao.addTrack(playlistId, 20, 3))
        Assert.assertTrue(dao.addTrack(playlistId, 30, 4))
        Assert.assertFalse(dao.addTrack(playlistId, 20, 5))
        dao.moveTrack(playlistId, fromIndex = 2, toIndex = 0, now = 6)

        Assert.assertEquals(listOf(30L, 10L, 20L), dao.tracksOnce(playlistId).map { it.trackId })
    }

    @Test
    fun eliminarPlaylistEliminaSusRelacionesEnCascada() = runBlocking {
        val dao = database.playlistDao()
        val playlistId = dao.insertPlaylist(
            com.nightcallaudio.data.database.entity.PlaylistEntity(name = "Temporal", createdAtEpochMs = 1, updatedAtEpochMs = 1),
        )
        dao.addTrack(playlistId, 10, 2)

        dao.delete(playlistId)

        Assert.assertTrue(dao.tracksOnce(playlistId).isEmpty())
    }

    @Test
    fun favoritosNoSeDuplican() = runBlocking {
        val dao = database.favoritesDao()
        dao.insert(FavoriteEntity(42, 1))
        dao.insert(FavoriteEntity(42, 2))

        Assert.assertEquals(listOf(42L), dao.observeIds().first())
    }

    @Test
    fun sesionPersisteColaConDuplicadosYEstado() = runBlocking {
        val repository = RoomPlaybackPersistenceRepository(database) { 100 }
        val session = PersistedPlaybackSession(
            trackIds = listOf(7, 8, 7),
            originalPositions = listOf(0, 1, 2),
            currentIndex = 1,
            positionMs = 12_345,
            shuffleEnabled = true,
            repeatMode = RepeatMode.ALL,
        )

        repository.save(session)

        Assert.assertEquals(session, repository.observe().first())
    }
}
