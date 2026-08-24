package com.nightcallaudio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.playback.PlaybackController
import com.nightcallaudio.ui.library.LibraryUiState
import com.nightcallaudio.ui.library.LibraryViewModel
import com.nightcallaudio.ui.theme.NightcallAudioTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { NightcallAudioTheme { NightcallAudioApp() } }
    }
}

@Composable
private fun NightcallAudioApp(viewModel: LibraryViewModel = viewModel()) {
    val context = LocalContext.current
    val permission = remember { audioPermission() }
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        if (granted) viewModel.loadMusic()
    }
    val playbackController = remember { PlaybackController(context) }
    DisposableEffect(playbackController) { onDispose(playbackController::close) }

    if (hasPermission) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val query by viewModel.query.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { viewModel.loadMusic() }
        LibraryScreen(
            uiState = uiState,
            query = query,
            onQueryChange = viewModel::updateQuery,
            onRefresh = { viewModel.loadMusic(force = true) },
            onTrackClick = { index -> playbackController.play(uiState.tracks, index) },
        )
    } else {
        PermissionScreen { permissionLauncher.launch(permission) }
    }
}

@Composable
private fun PermissionScreen(onRequestPermission: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.permission_audio_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.permission_audio_message), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRequestPermission) { Text(stringResource(R.string.permission_audio_action)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(
    uiState: LibraryUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onTrackClick: (Int) -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("NightcallAudio", fontWeight = FontWeight.Bold) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar canción, artista o álbum") },
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                uiState.errorMessage != null -> EmptyState("No se pudo cargar la biblioteca", uiState.errorMessage, "Reintentar", onRefresh)
                uiState.tracks.isEmpty() -> EmptyState(
                    if (query.isBlank()) "No hay música" else "Sin resultados",
                    if (query.isBlank()) "Añade canciones de al menos 30 segundos al dispositivo." else "Prueba con otro título, artista o álbum.",
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(uiState.tracks, key = { _, track -> track.id }) { index, track ->
                        TrackRow(track) { onTrackClick(index) }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(track: Track, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { Text("♪", style = MaterialTheme.typography.headlineMedium) }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text("${track.artist} · ${track.album}", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
            }
            Text(formatDuration(track.durationMs), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun EmptyState(title: String, message: String, action: String? = null, onAction: () -> Unit = {}) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge)
            if (action != null) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAction) { Text(action) }
            }
        }
    }
}

private fun audioPermission(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1_000
    return String.format(Locale.getDefault(), "%d:%02d", seconds / 60, seconds % 60)
}
