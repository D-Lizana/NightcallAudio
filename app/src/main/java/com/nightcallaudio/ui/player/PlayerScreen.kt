package com.nightcallaudio.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.nightcallaudio.domain.model.PlaybackState
import com.nightcallaudio.domain.model.PlaybackStatus
import com.nightcallaudio.ui.components.MessageState
import com.nightcallaudio.ui.components.TrackList
import com.nightcallaudio.ui.components.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    state: PlaybackState,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeek: (Long) -> Unit,
    onOpenQueue: () -> Unit,
) {
    val track = state.currentTrack
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reproduciendo") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver") } },
                actions = { IconButton(onClick = onOpenQueue) { Icon(Icons.AutoMirrored.Rounded.QueueMusic, "Abrir cola") } },
            )
        },
    ) { padding ->
        if (track == null) {
            MessageState("Nada en reproducción", "Selecciona una canción de tu biblioteca.", Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 6.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null, modifier = Modifier.size(112.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    if (track.artworkUri != null) {
                        AsyncImage(
                            model = track.artworkUri,
                            contentDescription = "Carátula de ${track.album}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Slider(
                value = state.positionMs.toFloat().coerceAtMost(state.durationMs.coerceAtLeast(1).toFloat()),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(state.positionMs), style = MaterialTheme.typography.labelMedium)
                Text(formatDuration(state.durationMs), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(12.dp))
            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onPrevious) { Icon(Icons.Rounded.SkipPrevious, "Anterior") }
                IconButton(onClick = onSeekBack) { Icon(Icons.Rounded.Replay10, "Retroceder 10 segundos") }
                FilledIconButton(onClick = onPlayPause, modifier = Modifier.size(72.dp)) {
                    if (state.status == PlaybackStatus.BUFFERING) {
                        CircularProgressIndicator(Modifier.size(30.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (state.isPlaying) "Pausar" else "Reproducir", modifier = Modifier.size(42.dp))
                    }
                }
                IconButton(onClick = onSeekForward) { Icon(Icons.Rounded.Forward10, "Avanzar 10 segundos") }
                IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, "Siguiente") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(state: PlaybackState, onBack: () -> Unit, onTrackClick: (Int) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cola de reproducción", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver") } },
            )
        },
    ) { padding ->
        if (state.queue.isEmpty()) {
            MessageState("La cola está vacía", "Selecciona música para empezar.", Modifier.padding(padding))
        } else {
            TrackList(state.queue, onTrackClick, Modifier.padding(padding).padding(horizontal = 16.dp))
        }
    }
}
