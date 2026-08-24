package com.nightcallaudio.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nightcallaudio.ui.components.MessageState
import com.nightcallaudio.ui.components.TrackList
import com.nightcallaudio.ui.library.LibraryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: LibraryUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onTrackClick: (Int) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Buscar", fontWeight = FontWeight.Bold) })
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Canción, artista o álbum") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        when {
            query.isBlank() -> MessageState("Busca en tu biblioteca", "Los resultados se filtran localmente en el dispositivo.")
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.searchResults.isEmpty() -> MessageState("Sin resultados", "Prueba con otro título, artista o álbum.")
            else -> TrackList(state.searchResults, onTrackClick, Modifier.padding(horizontal = 16.dp))
        }
    }
}
