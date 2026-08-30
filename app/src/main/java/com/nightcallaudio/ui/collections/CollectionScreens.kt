package com.nightcallaudio.ui.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.nightcallaudio.R
import com.nightcallaudio.domain.model.Playlist
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.ui.components.MessageState
import com.nightcallaudio.ui.components.TrackList
import com.nightcallaudio.ui.components.TrackRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(state: CollectionsUiState, onCreate: (String) -> Unit, onRename: (Long, String) -> Unit, onDelete: (Long) -> Unit, onOpen: (Long) -> Unit) {
    var editPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deletePlaylist by remember { mutableStateOf<Playlist?>(null) }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.navigation_playlists), fontWeight = FontWeight.Bold) }, actions = { IconButton({ creating = true }) { Icon(Icons.Rounded.Add, stringResource(R.string.create_playlist)) } })
        state.errorMessage?.let { Text(it, Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.error) }
        if (state.playlists.isEmpty()) {
            MessageState(stringResource(R.string.no_playlists), stringResource(R.string.playlists_explanation), action = stringResource(R.string.create_playlist), onAction = { creating = true })
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.playlists, key = Playlist::id) { playlist ->
                    var menu by remember { mutableStateOf(false) }
                    ElevatedCard(Modifier.fillMaxWidth().clickable { onOpen(playlist.id) }) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Rounded.QueueMusic, null)
                            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                                Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.song_count, playlist.tracks.size), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box {
                                IconButton({ menu = true }) { Icon(Icons.Rounded.MoreVert, stringResource(R.string.options)) }
                                DropdownMenu(menu, { menu = false }) {
                                    DropdownMenuItem({ Text(stringResource(R.string.rename_action)) }, { menu = false; editPlaylist = playlist })
                                    DropdownMenuItem({ Text(stringResource(R.string.delete_action)) }, { menu = false; deletePlaylist = playlist })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (creating || editPlaylist != null) PlaylistNameDialog(
        if (creating) stringResource(R.string.create_playlist) else stringResource(R.string.rename_playlist),
        editPlaylist?.name.orEmpty(),
        { creating = false; editPlaylist = null },
    ) { name ->
        editPlaylist?.let { onRename(it.id, name) } ?: onCreate(name)
        creating = false; editPlaylist = null
    }
    deletePlaylist?.let { playlist ->
        AlertDialog(
            onDismissRequest = { deletePlaylist = null },
            title = { Text(stringResource(R.string.delete_playlist)) },
            text = { Text(stringResource(R.string.delete_playlist_message, playlist.name)) },
            confirmButton = { TextButton({ onDelete(playlist.id); deletePlaylist = null }) { Text(stringResource(R.string.delete_action)) } },
            dismissButton = { TextButton({ deletePlaylist = null }) { Text(stringResource(R.string.cancel_action)) } },
        )
    }
}

@Composable
private fun PlaylistNameDialog(title: String, initialName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.name_label)) }, singleLine = true) },
        confirmButton = { TextButton({ onConfirm(name) }, enabled = name.isNotBlank()) { Text(stringResource(R.string.save_action)) } },
        dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.cancel_action)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(tracks: List<Track>, onPlayTracks: (List<Track>, Int) -> Unit, onToggleFavorite: (Long) -> Unit, onPlayNext: (Track) -> Unit, onAddToQueue: (Track) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.navigation_favorites), fontWeight = FontWeight.Bold) })
        if (tracks.isEmpty()) MessageState(stringResource(R.string.no_favorites), stringResource(R.string.favorites_explanation))
        else TrackList(tracks, { onPlayTracks(tracks, it) }, Modifier.padding(horizontal = 16.dp), onPlayNext, onAddToQueue, { true }, { onToggleFavorite(it.id) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(playlist: Playlist?, onBack: () -> Unit, onPlayTracks: (List<Track>, Int) -> Unit, onRemove: (Long) -> Unit, onMove: (Int, Int) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(playlist?.name ?: stringResource(R.string.navigation_playlists), fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back_action)) } }) }) { padding ->
        val tracks = playlist?.tracks.orEmpty()
        if (tracks.isEmpty()) MessageState(stringResource(R.string.empty_playlist), stringResource(R.string.empty_playlist_explanation), Modifier.padding(padding))
        else LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                TrackRow(track, { onPlayTracks(tracks, index) })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton({ if (index > 0) onMove(index, index - 1) }, enabled = index > 0) { Icon(Icons.Rounded.KeyboardArrowUp, stringResource(R.string.move_up)) }
                    IconButton({ if (index < tracks.lastIndex) onMove(index, index + 1) }, enabled = index < tracks.lastIndex) { Icon(Icons.Rounded.KeyboardArrowDown, stringResource(R.string.move_down)) }
                    IconButton({ onRemove(track.id) }) { Icon(Icons.Rounded.Delete, stringResource(R.string.remove_playlist)) }
                }
            }
        }
    }
}
