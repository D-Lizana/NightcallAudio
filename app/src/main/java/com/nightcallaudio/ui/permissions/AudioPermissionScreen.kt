package com.nightcallaudio.ui.permissions

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.nightcallaudio.R
import com.nightcallaudio.permissions.AudioPermissionState

@Composable
fun AudioPermissionScreen(
    state: AudioPermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val permanentlyDenied = state == AudioPermissionState.PERMANENTLY_DENIED
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(88.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (permanentlyDenied) Icons.Rounded.Settings else Icons.Rounded.LibraryMusic,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(if (permanentlyDenied) R.string.permission_settings_title else R.string.permission_audio_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                when (state) {
                    AudioPermissionState.DENIED -> stringResource(R.string.permission_denied_message)
                    AudioPermissionState.PERMANENTLY_DENIED -> stringResource(R.string.permission_permanently_denied_message)
                    else -> stringResource(R.string.permission_initial_message)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = if (permanentlyDenied) onOpenSettings else onRequestPermission) {
                Icon(if (permanentlyDenied) Icons.Rounded.Settings else Icons.Rounded.LibraryMusic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (permanentlyDenied) R.string.open_settings else R.string.permission_audio_action))
            }
            if (state == AudioPermissionState.DENIED) {
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.permission_later), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
