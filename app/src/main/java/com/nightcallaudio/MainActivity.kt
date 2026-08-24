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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nightcallaudio.ui.library.LibraryViewModel
import com.nightcallaudio.ui.navigation.NightcallNavigation
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
    val container = remember { (context.applicationContext as NightcallAudioApplication).container }
    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModel.factory(container.getMusicLibrary, container.searchTracks),
    )
    val permission = remember { audioPermission() }
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        if (granted) libraryViewModel.loadMusic()
    }

    if (hasPermission) {
        LaunchedEffect(Unit) { libraryViewModel.loadMusic() }
        NightcallNavigation(libraryViewModel, container.playbackRepository)
    } else {
        PermissionScreen { launcher.launch(permission) }
    }
}

@Composable
private fun PermissionScreen(onRequestPermission: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.permission_audio_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.permission_audio_message), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRequestPermission) { Text(stringResource(R.string.permission_audio_action)) }
        }
    }
}

private fun audioPermission(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_AUDIO
} else {
    Manifest.permission.READ_EXTERNAL_STORAGE
}
