package com.nightcallaudio.ui.library

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.model.Playlist
import com.nightcallaudio.ui.components.MessageState
import com.nightcallaudio.ui.components.TrackList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    title: String,
    subtitle: String,
    tracks: List<Track>,
    onBack: () -> Unit,
    onPlayTracks: (List<Track>, Int) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    favoriteIds: Set<Long>,
    playlists: List<Playlist>,
    onToggleFavorite: (Track) -> Unit,
    onAddToPlaylist: (Long, Track) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Column {
                        Text(title, fontWeight = FontWeight.Bold)
                        Text(subtitle, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        if (tracks.isEmpty()) {
            MessageState("Sin canciones", "Esta colección ya no contiene canciones disponibles.", Modifier.padding(padding))
        } else {
            TrackList(
                tracks = tracks,
                onTrackClick = { onPlayTracks(tracks, it) },
                modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
                onPlayNext = onPlayNext,
                onAddToQueue = onAddToQueue,
                isFavorite = { it.id in favoriteIds },
                onToggleFavorite = onToggleFavorite,
                playlists = playlists,
                onAddToPlaylist = onAddToPlaylist,
            )
        }
    }
}
