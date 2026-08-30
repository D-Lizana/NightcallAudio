package com.nightcallaudio.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.nightcallaudio.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import coil3.compose.AsyncImage
import com.nightcallaudio.domain.model.PlaybackState
import com.nightcallaudio.domain.model.PlaybackStatus
import com.nightcallaudio.domain.model.RepeatMode
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
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSeek: (Long) -> Unit,
    onOpenQueue: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    val track = state.currentTrack
    var isSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(state.positionMs.toFloat()) }
    LaunchedEffect(state.positionMs, isSeeking) {
        if (!isSeeking) sliderPosition = state.positionMs.toFloat()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.player_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back_action)) } },
                actions = {
                    if (track != null) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                stringResource(if (isFavorite) R.string.remove_favorites else R.string.add_favorites),
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(onClick = onOpenQueue) {
                        Icon(Icons.AutoMirrored.Rounded.QueueMusic, stringResource(R.string.open_queue))
                    }
                },
            )
        },
    ) { padding ->
        if (track == null) {
            MessageState(stringResource(R.string.nothing_playing), stringResource(R.string.select_song), Modifier.padding(padding))
            return@Scaffold
        }
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            val artworkSize = minOf(maxWidth - 56.dp, 480.dp)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                modifier = Modifier.size(artworkSize.coerceAtLeast(180.dp)),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 6.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null, modifier = Modifier.size(112.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
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
            Spacer(Modifier.height(28.dp))
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                IconButton(onClick = onToggleShuffle) {
                    BadgedBox(badge = { if (state.shuffleEnabled) Badge { Text("✓") } }) {
                        Icon(
                            Icons.Rounded.Shuffle,
                            stringResource(if (state.shuffleEnabled) R.string.shuffle_on else R.string.shuffle_off),
                            tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onCycleRepeat) {
                    BadgedBox(badge = { Badge { Text(when (state.repeatMode) { RepeatMode.OFF -> "No"; RepeatMode.ALL -> "∞"; RepeatMode.ONE -> "1" }) } }) {
                        Icon(
                            if (state.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                            when (state.repeatMode) {
                                RepeatMode.OFF -> stringResource(R.string.repeat_off)
                                RepeatMode.ALL -> stringResource(R.string.repeat_all)
                                RepeatMode.ONE -> stringResource(R.string.repeat_one)
                            },
                            tint = if (state.repeatMode == RepeatMode.OFF) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
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
            Spacer(Modifier.height(20.dp))
            Slider(
                value = sliderPosition.coerceIn(0f, state.durationMs.coerceAtLeast(1).toFloat()),
                onValueChange = {
                    isSeeking = true
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    onSeek(sliderPosition.toLong())
                    isSeeking = false
                },
                valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(if (isSeeking) sliderPosition.toLong() else state.positionMs), style = MaterialTheme.typography.labelMedium)
                Text(formatDuration(state.durationMs), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    state: PlaybackState,
    onBack: () -> Unit,
    onTrackClick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.queue_title), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back_action)) } },
            )
        },
    ) { padding ->
        if (state.queue.isEmpty()) {
            MessageState(stringResource(R.string.empty_queue), stringResource(R.string.select_music), Modifier.padding(padding))
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                Modifier.padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.queue.size, key = { index -> "$index-${state.queue[index].id}" }) { index ->
                    val dismissState = rememberSwipeToDismissBoxState()
                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                            onRemove(index)
                            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                        }
                    }
                    var draggedY by remember { mutableFloatStateOf(0f) }
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Box(Modifier.fillMaxSize().padding(end = 20.dp), contentAlignment = Alignment.CenterEnd) {
                                Icon(Icons.Rounded.Delete, "Eliminar de la cola", tint = MaterialTheme.colorScheme.error)
                            }
                        },
                    ) {
                        com.nightcallaudio.ui.components.TrackRow(
                            track = state.queue[index],
                            onClick = { onTrackClick(index) },
                            modifier = Modifier
                                .semantics {
                                    customActions = buildList {
                                        if (index > 0) add(CustomAccessibilityAction("Subir en la cola") { onMove(index, index - 1); true })
                                        if (index < state.queue.lastIndex) add(CustomAccessibilityAction("Bajar en la cola") { onMove(index, index + 1); true })
                                        add(CustomAccessibilityAction("Eliminar de la cola") { onRemove(index); true })
                                    }
                                }
                                .pointerInput(index, state.queue.size) {
                                detectDragGesturesAfterLongPress(
                                    onDragEnd = { draggedY = 0f },
                                    onDragCancel = { draggedY = 0f },
                                ) { change, dragAmount ->
                                    change.consume()
                                    draggedY += dragAmount.y
                                    val threshold = 52.dp.toPx()
                                    when {
                                        draggedY > threshold && index < state.queue.lastIndex -> {
                                            onMove(index, index + 1)
                                            draggedY = 0f
                                        }
                                        draggedY < -threshold && index > 0 -> {
                                            onMove(index, index - 1)
                                            draggedY = 0f
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}
