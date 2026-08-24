package com.nightcallaudio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.permissions.AudioPermissionState
import com.nightcallaudio.permissions.PermissionPolicy
import com.nightcallaudio.permissions.PermissionRequestStore
import com.nightcallaudio.ui.library.LibraryViewModel
import com.nightcallaudio.ui.navigation.NightcallNavigation
import com.nightcallaudio.ui.permissions.AudioPermissionScreen
import com.nightcallaudio.ui.theme.NightcallAudioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { NightcallAudioTheme { NightcallAudioApp() } }
    }
}

@Composable
private fun NightcallAudioApp() {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val container = remember { (context.applicationContext as NightcallAudioApplication).container }
    val requestStore = remember { PermissionRequestStore(context) }
    val audioPermission = remember { PermissionPolicy.audioPermission() }
    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModel.factory(container.getMusicLibrary, container.searchTracks, container.cleanupMissingReferences),
    )

    fun currentAudioState(): AudioPermissionState = PermissionPolicy.evaluateAudioPermission(
        granted = ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED,
        wasRequested = requestStore.audioWasRequested,
        shouldShowRationale = activity.shouldShowRequestPermissionRationale(audioPermission),
    )

    var audioState by remember { mutableStateOf(currentAudioState()) }
    var pendingPlayback by remember { mutableStateOf<Pair<List<Track>, Int>?>(null) }

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        audioState = currentAudioState()
        if (audioState == AudioPermissionState.GRANTED) libraryViewModel.loadMusic(force = true)
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        pendingPlayback?.let { (tracks, index) -> container.playbackRepository.play(tracks, index) }
        pendingPlayback = null
    }

    DisposableEffect(lifecycleOwner, audioPermission) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                audioState = currentAudioState()
                if (audioState == AudioPermissionState.GRANTED) libraryViewModel.loadMusic()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (audioState == AudioPermissionState.GRANTED) {
        LaunchedEffect(Unit) { libraryViewModel.loadMusic() }
        NightcallNavigation(
            libraryViewModel = libraryViewModel,
            playbackRepository = container.playbackRepository,
            onPlayTracks = { tracks, index ->
                val shouldRequestNotifications = PermissionPolicy.requiresNotificationPermission() &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                    !requestStore.notificationsWereRequested
                if (shouldRequestNotifications) {
                    requestStore.notificationsWereRequested = true
                    pendingPlayback = tracks to index
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    container.playbackRepository.play(tracks, index)
                }
            },
        )
    } else {
        AudioPermissionScreen(
            state = audioState,
            onRequestPermission = {
                requestStore.audioWasRequested = true
                audioLauncher.launch(audioPermission)
            },
            onOpenSettings = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    ),
                )
            },
        )
    }
}
