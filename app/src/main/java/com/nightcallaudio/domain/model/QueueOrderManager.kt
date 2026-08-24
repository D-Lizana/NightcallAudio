package com.nightcallaudio.domain.model

import kotlin.random.Random

class QueueOrderManager(
    private val random: Random = Random.Default,
) {
    private data class Entry(val instanceId: Long, val track: Track)

    private var nextInstanceId = 0L
    private var canonical = mutableListOf<Entry>()
    private var active = mutableListOf<Entry>()
    var shuffleEnabled: Boolean = false
        private set

    val tracks: List<Track>
        get() = active.map(Entry::track)

    fun replace(tracks: List<Track>) {
        canonical = tracks.map { Entry(nextInstanceId++, it) }.toMutableList()
        active = canonical.toMutableList()
        shuffleEnabled = false
    }

    fun setShuffleEnabled(enabled: Boolean, currentIndex: Int): Int {
        if (enabled == shuffleEnabled || active.isEmpty()) return currentIndex.coerceInActiveRange()
        val current = active.getOrNull(currentIndex)
        active = if (enabled) {
            val remaining = canonical.filterNot { it.instanceId == current?.instanceId }.shuffled(random)
            (listOfNotNull(current) + remaining).toMutableList()
        } else {
            canonical.toMutableList()
        }
        shuffleEnabled = enabled
        return current?.let { entry -> active.indexOfFirst { it.instanceId == entry.instanceId } }?.takeIf { it >= 0 } ?: 0
    }

    fun addNext(track: Track, currentIndex: Int): Int {
        val entry = Entry(nextInstanceId++, track)
        val activeInsertion = (currentIndex + 1).coerceIn(0, active.size)
        active.add(activeInsertion, entry)
        val currentEntry = active.getOrNull(currentIndex)
        val canonicalInsertion = currentEntry?.let { current ->
            canonical.indexOfFirst { it.instanceId == current.instanceId }.takeIf { it >= 0 }?.plus(1)
        } ?: canonical.size
        canonical.add(canonicalInsertion.coerceIn(0, canonical.size), entry)
        return activeInsertion
    }

    fun addToEnd(track: Track, currentIndex: Int): Int {
        val entry = Entry(nextInstanceId++, track)
        canonical.add(entry)
        val insertion = if (shuffleEnabled) {
            random.nextInt((currentIndex + 1).coerceAtLeast(0), active.size + 1)
        } else {
            active.size
        }
        active.add(insertion, entry)
        return insertion
    }

    fun remove(index: Int) {
        val removed = active.removeAt(index)
        canonical.removeAll { it.instanceId == removed.instanceId }
    }

    fun move(fromIndex: Int, toIndex: Int) {
        require(fromIndex in active.indices && toIndex in active.indices)
        if (fromIndex == toIndex) return
        active.add(toIndex, active.removeAt(fromIndex))
        if (!shuffleEnabled) canonical = active.toMutableList()
    }

    private fun Int.coerceInActiveRange(): Int = if (active.isEmpty()) -1 else coerceIn(0, active.lastIndex)
}
