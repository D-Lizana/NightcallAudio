package com.nightcallaudio

import com.nightcallaudio.domain.model.QueueOrderManager
import com.nightcallaudio.domain.model.Track
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueOrderManagerTest {
    private val first = track(1, "Primera")
    private val second = track(2, "Segunda")
    private val third = track(3, "Tercera")

    @Test
    fun `shuffle conserva la actual y visita cada elemento una vez`() {
        val manager = QueueOrderManager(Random(7))
        manager.replace(listOf(first, second, third))

        val currentIndex = manager.setShuffleEnabled(true, 1)

        assertEquals(0, currentIndex)
        assertEquals(second, manager.tracks.first())
        assertEquals(listOf(1L, 2L, 3L), manager.tracks.map(Track::id).sorted())
    }

    @Test
    fun `desactivar shuffle restaura orden y mantiene cancion actual`() {
        val manager = QueueOrderManager(Random(4))
        manager.replace(listOf(first, second, third))
        manager.setShuffleEnabled(true, 1)
        val current = manager.tracks[2]

        val restoredIndex = manager.setShuffleEnabled(false, 2)

        assertEquals(listOf(first, second, third), manager.tracks)
        assertEquals(current, manager.tracks[restoredIndex])
    }

    @Test
    fun `admite duplicados como instancias independientes`() {
        val manager = QueueOrderManager(Random(1))
        manager.replace(listOf(first, first, second))
        manager.setShuffleEnabled(true, 0)

        manager.remove(manager.tracks.indexOfFirst { it.id == first.id })

        assertEquals(2, manager.tracks.size)
        assertTrue(manager.tracks.any { it.id == first.id })
    }

    @Test
    fun `reproducir siguiente inserta inmediatamente tras la actual`() {
        val manager = QueueOrderManager(Random(1))
        manager.replace(listOf(first, third))

        val insertedIndex = manager.addNext(second, 0)

        assertEquals(1, insertedIndex)
        assertEquals(listOf(first, second, third), manager.tracks)
    }

    @Test
    fun `mover en modo normal actualiza el orden restaurable`() {
        val manager = QueueOrderManager(Random(1))
        manager.replace(listOf(first, second, third))
        manager.move(2, 0)
        manager.setShuffleEnabled(true, 0)
        manager.setShuffleEnabled(false, 0)

        assertEquals(listOf(third, first, second), manager.tracks)
    }

    @Test
    fun `restaura orden activo y recupera el canonico`() {
        val manager = QueueOrderManager(Random(1))
        manager.restore(
            tracks = listOf(second, third, first),
            originalPositions = listOf(1, 2, 0),
            shuffled = true,
        )

        assertEquals(listOf(1, 2, 0), manager.originalPositions)
        manager.setShuffleEnabled(false, 0)
        assertEquals(listOf(first, second, third), manager.tracks)
    }

    @Test
    fun `anadir al final durante shuffle conserva todas las instancias`() {
        val manager = QueueOrderManager(Random(3))
        manager.replace(listOf(first, second))
        manager.setShuffleEnabled(true, 0)

        manager.addToEnd(third, 0)
        manager.setShuffleEnabled(false, 0)

        assertEquals(listOf(first, second, third), manager.tracks)
    }

    @Test
    fun `cola vacia usa indice menos uno`() {
        val manager = QueueOrderManager(Random(1))
        manager.replace(emptyList())

        assertEquals(-1, manager.setShuffleEnabled(true, 4))
        assertTrue(manager.tracks.isEmpty())
    }

    private fun track(id: Long, title: String) = Track(
        id = id,
        contentUri = "content://audio/$id",
        title = title,
        artist = "Artista",
        artistId = 1,
        album = "Álbum",
        albumId = 1,
        artworkUri = null,
        durationMs = 180_000,
        trackNumber = id.toInt(),
        discNumber = 1,
        genre = null,
        folder = "Music",
        year = 2026,
        dateAddedEpochSeconds = 1,
    )
}
