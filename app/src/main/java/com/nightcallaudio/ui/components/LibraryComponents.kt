package com.nightcallaudio.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import com.nightcallaudio.R
import coil3.compose.AsyncImage
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.model.Playlist
import java.util.Locale

@Composable
fun TrackList(
    tracks: List<Track>,
    onTrackClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onPlayNext: ((Track) -> Unit)? = null,
    onAddToQueue: ((Track) -> Unit)? = null,
    isFavorite: ((Track) -> Boolean)? = null,
    onToggleFavorite: ((Track) -> Unit)? = null,
    playlists: List<Playlist> = emptyList(),
    onAddToPlaylist: ((Long, Track) -> Unit)? = null,
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            TrackRow(
                track = track,
                onClick = { onTrackClick(index) },
                onPlayNext = onPlayNext?.let { action -> { action(track) } },
                onAddToQueue = onAddToQueue?.let { action -> { action(track) } },
                isFavorite = isFavorite?.invoke(track),
                onToggleFavorite = onToggleFavorite?.let { action -> { action(track) } },
                playlists = playlists,
                onAddToPlaylist = onAddToPlaylist?.let { action -> { playlistId -> action(playlistId, track) } },
            )
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPlayNext: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    isFavorite: Boolean? = null,
    onToggleFavorite: (() -> Unit)? = null,
    playlists: List<Playlist> = emptyList(),
    onAddToPlaylist: ((Long) -> Unit)? = null,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth().clickable(
            role = Role.Button,
            onClickLabel = stringResource(R.string.play_track, track.title),
            onClick = onClick,
        ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    if (track.artworkUri != null) {
                        AsyncImage(
                            model = track.artworkUri,
                            contentDescription = stringResource(R.string.album_cover, track.album),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${track.artist} · ${track.album}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(formatDuration(track.durationMs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (onPlayNext != null || onAddToQueue != null || onToggleFavorite != null || (playlists.isNotEmpty() && onAddToPlaylist != null)) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Rounded.MoreVert, stringResource(R.string.more_options)) }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        if (onPlayNext != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.play_next)) },
                                onClick = { menuExpanded = false; onPlayNext() },
                            )
                        }
                        if (onAddToQueue != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_queue_end)) },
                                onClick = { menuExpanded = false; onAddToQueue() },
                            )
                        }
                        if (onToggleFavorite != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(if (isFavorite == true) R.string.remove_favorites else R.string.add_favorites)) },
                                leadingIcon = { Icon(if (isFavorite == true) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null) },
                                onClick = { menuExpanded = false; onToggleFavorite() },
                            )
                        }
                        if (onAddToPlaylist != null) playlists.forEach { playlist ->
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_to_playlist, playlist.name)) },
                                onClick = { menuExpanded = false; onAddToPlaylist(playlist.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            if (action != null) {
                Spacer(Modifier.height(18.dp))
                Button(onClick = onAction) { Text(action) }
            }
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val seconds = durationMs.coerceAtLeast(0L) / 1_000
    return String.format(Locale.getDefault(), "%d:%02d", seconds / 60, seconds % 60)
}
