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
                if (permanentlyDenied) "Activa el acceso desde Ajustes" else "Acceso a tu música",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                when (state) {
                    AudioPermissionState.DENIED -> "Sin este permiso no podemos encontrar las canciones guardadas en el dispositivo. No se accederá a fotos ni a otros archivos."
                    AudioPermissionState.PERMANENTLY_DENIED -> "Android ya no permite solicitar el permiso desde esta pantalla. Abre los ajustes de NightcallAudio y permite el acceso a música y audio."
                    else -> "NightcallAudio necesita permiso para encontrar y reproducir los archivos de música del dispositivo. Los datos permanecerán en el teléfono."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = if (permanentlyDenied) onOpenSettings else onRequestPermission) {
                Icon(if (permanentlyDenied) Icons.Rounded.Settings else Icons.Rounded.LibraryMusic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (permanentlyDenied) "Abrir Ajustes" else "Permitir acceso")
            }
            if (state == AudioPermissionState.DENIED) {
                Spacer(Modifier.height(10.dp))
                Text("Puedes cambiar esta decisión más adelante desde Ajustes.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
