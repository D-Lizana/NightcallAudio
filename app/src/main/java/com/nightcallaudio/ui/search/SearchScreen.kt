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
import androidx.compose.ui.res.stringResource
import com.nightcallaudio.R
import com.nightcallaudio.ui.components.MessageState
import com.nightcallaudio.ui.components.TrackList
import com.nightcallaudio.ui.library.LibraryUiState
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.model.Playlist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: LibraryUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onTrackClick: (Int) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    favoriteIds: Set<Long>,
    playlists: List<Playlist>,
    onToggleFavorite: (Track) -> Unit,
    onAddToPlaylist: (Long, Track) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.navigation_search), fontWeight = FontWeight.Bold) })
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.errorMessage != null -> MessageState(stringResource(R.string.search_failed), state.errorMessage)
            query.isBlank() -> MessageState(stringResource(R.string.search_library), stringResource(R.string.search_local_explanation))
            state.searchResults.isEmpty() -> MessageState(stringResource(R.string.no_results), stringResource(R.string.no_results_explanation))
            else -> TrackList(
                state.searchResults,
                onTrackClick,
                Modifier.padding(horizontal = 16.dp),
                onPlayNext,
                onAddToQueue,
                { it.id in favoriteIds }, onToggleFavorite, playlists, onAddToPlaylist,
            )
        }
    }
}
