package com.nightcallaudio.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nightcallaudio.ui.components.MessageState
import com.nightcallaudio.ui.components.TrackList
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.model.Playlist

private enum class LibrarySection(val label: String) {
    TRACKS("Canciones"),
    ARTISTS("Artistas"),
    ALBUMS("Álbumes"),
    GENRES("Géneros"),
    FOLDERS("Carpetas"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onRefresh: () -> Unit,
    onPlayTracks: (List<Track>, Int) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenAlbum: (String, String) -> Unit,
    favoriteIds: Set<Long>,
    playlists: List<Playlist>,
    onToggleFavorite: (Track) -> Unit,
    onAddToPlaylist: (Long, Track) -> Unit,
) {
    var section by rememberSaveable { mutableStateOf(LibrarySection.TRACKS) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text("Biblioteca", fontWeight = FontWeight.Bold)
                }
            },
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(LibrarySection.entries) { item ->
                FilterChip(
                    selected = section == item,
                    onClick = {
                        section = item
                        selectedCategory = null
                    },
                    label = { Text(item.label) },
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.errorMessage != null -> MessageState("No se pudo cargar la biblioteca", state.errorMessage, action = "Reintentar", onAction = onRefresh)
            state.tracks.isEmpty() -> MessageState("No hay música", "No se han encontrado canciones en el dispositivo")
            section == LibrarySection.TRACKS -> TrackList(
                state.tracks,
                { index -> onPlayTracks(state.tracks, index) },
                Modifier.padding(horizontal = 16.dp),
                onPlayNext,
                onAddToQueue,
                { it.id in favoriteIds }, onToggleFavorite, playlists, onAddToPlaylist,
            )
            selectedCategory != null -> {
                val categoryTracks = state.tracks.forCategory(section, selectedCategory.orEmpty())
                Column(Modifier.fillMaxSize()) {
                    TextButton(onClick = { selectedCategory = null }, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text("‹ ${section.label} · ${selectedCategory.orEmpty()}")
                    }
                    TrackList(
                        categoryTracks,
                        { index -> onPlayTracks(categoryTracks, index) },
                        Modifier.padding(horizontal = 16.dp),
                        onPlayNext,
                        onAddToQueue,
                        { it.id in favoriteIds }, onToggleFavorite, playlists, onAddToPlaylist,
                    )
                }
            }
            else -> CategoryPreview(
                section = section,
                state = state,
                onSelect = { value, secondary ->
                    when (section) {
                        LibrarySection.ARTISTS -> onOpenArtist(value)
                        LibrarySection.ALBUMS -> onOpenAlbum(value, secondary.orEmpty())
                        else -> selectedCategory = value
                    }
                },
            )
        }
    }
}

@Composable
private fun CategoryPreview(
    section: LibrarySection,
    state: LibraryUiState,
    onSelect: (String, String?) -> Unit,
) {
    val entries = when (section) {
        LibrarySection.ARTISTS -> state.artists.map { CategoryEntry(it.name, it.trackCount) }
        LibrarySection.ALBUMS -> state.albums.map { CategoryEntry(it.title, it.trackCount, it.artist) }
        LibrarySection.GENRES -> state.genres.map { CategoryEntry(it.name, it.trackCount) }
        LibrarySection.FOLDERS -> state.folders.map { CategoryEntry(it.name, it.trackCount) }
        LibrarySection.TRACKS -> emptyList()
    }
    if (entries.isEmpty()) {
        MessageState("Sin ${section.label.lowercase()}", "No hay información disponible para esta categoría.")
    } else {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(entries.size) { index ->
                val entry = entries[index]
                ElevatedCard(Modifier.fillMaxWidth().clickable { onSelect(entry.title, entry.secondary) }) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.title, style = MaterialTheme.typography.titleMedium)
                            entry.secondary?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text("${entry.trackCount} canciones", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

private data class CategoryEntry(val title: String, val trackCount: Int, val secondary: String? = null)

private fun List<Track>.forCategory(section: LibrarySection, value: String): List<Track> {
    val matching = filter { track ->
        when (section) {
            LibrarySection.ARTISTS -> track.artist == value
            LibrarySection.ALBUMS -> track.album == value
            LibrarySection.GENRES -> track.genre == value
            LibrarySection.FOLDERS -> track.folder == value
            LibrarySection.TRACKS -> true
        }
    }
    return if (section == LibrarySection.ALBUMS) {
        matching.sortedWith(compareBy<Track>({ it.discNumber ?: 1 }, { it.trackNumber ?: Int.MAX_VALUE }, { it.title.lowercase() }))
    } else {
        matching.sortedBy { it.title.lowercase() }
    }
}
