package com.nightcallaudio.ui.library

import androidx.compose.foundation.layout.*
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
    onTrackClick: (Int) -> Unit,
) {
    var section by rememberSaveable { mutableStateOf(LibrarySection.TRACKS) }
    Column(Modifier.fillMaxSize()) {
        LargeTopAppBar(
            title = {
                Column {
                    Text("Biblioteca", fontWeight = FontWeight.Bold)
                    Text("Tu música local", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(LibrarySection.entries) { item ->
                FilterChip(selected = section == item, onClick = { section = item }, label = { Text(item.label) })
            }
        }
        Spacer(Modifier.height(10.dp))
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.errorMessage != null -> MessageState("No se pudo cargar la biblioteca", state.errorMessage, action = "Reintentar", onAction = onRefresh)
            state.tracks.isEmpty() -> MessageState("No hay música", "Añade canciones de al menos 30 segundos al dispositivo.")
            section == LibrarySection.TRACKS -> TrackList(state.tracks, onTrackClick, Modifier.padding(horizontal = 16.dp))
            else -> CategoryPreview(section = section, state = state)
        }
    }
}

@Composable
private fun CategoryPreview(section: LibrarySection, state: LibraryUiState) {
    val labels = when (section) {
        LibrarySection.ARTISTS -> state.tracks.map { it.artist }.distinct().sorted()
        LibrarySection.ALBUMS -> state.tracks.map { it.album }.distinct().sorted()
        LibrarySection.GENRES -> state.tracks.mapNotNull { it.genre }.distinct().sorted()
        LibrarySection.FOLDERS -> state.tracks.mapNotNull { it.folder }.distinct().sorted()
        LibrarySection.TRACKS -> emptyList()
    }
    if (labels.isEmpty()) {
        MessageState("Sin ${section.label.lowercase()}", "No hay información disponible para esta categoría.")
    } else {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(labels.size) { index ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Text(labels[index], Modifier.padding(18.dp), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
