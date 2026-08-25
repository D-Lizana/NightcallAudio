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

    @Test
    fun retirarCancionNormalizaLasPosicionesDePlaylist() = runBlocking {
        val dao = database.playlistDao()
        val playlistId = dao.insertPlaylist(
            com.nightcallaudio.data.database.entity.PlaylistEntity(name = "Orden", createdAtEpochMs = 1, updatedAtEpochMs = 1),
        )
        dao.addTrack(playlistId, 10, 2)
        dao.addTrack(playlistId, 20, 3)
        dao.addTrack(playlistId, 30, 4)

        Assert.assertTrue(dao.removeTrack(playlistId, 20, 5))

        val remaining = dao.tracksOnce(playlistId)
        Assert.assertEquals(listOf(10L, 30L), remaining.map { it.trackId })
        Assert.assertEquals(listOf(0, 1), remaining.map { it.position })
    }

    @Test
    fun limpiezaEliminaReferenciasAusentesDePlaylistsYFavoritos() = runBlocking {
        val playlistDao = database.playlistDao()
        val favoritesDao = database.favoritesDao()
        val playlistId = playlistDao.insertPlaylist(
            com.nightcallaudio.data.database.entity.PlaylistEntity(name = "Limpieza", createdAtEpochMs = 1, updatedAtEpochMs = 1),
        )
        playlistDao.addTrack(playlistId, 10, 2)
        playlistDao.addTrack(playlistId, 20, 3)
        favoritesDao.insert(FavoriteEntity(10, 1))
        favoritesDao.insert(FavoriteEntity(20, 2))

        playlistDao.cleanupMissingTracks(setOf(20))
        favoritesDao.deleteMissing(setOf(20))

        Assert.assertEquals(listOf(20L), playlistDao.tracksOnce(playlistId).map { it.trackId })
        Assert.assertEquals(listOf(20L), favoritesDao.observeIds().first())
    }
}
