package com.nightcallaudio.ui.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.nightcallaudio.ui.components.MessageState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen() {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Playlists", fontWeight = FontWeight.Bold) },
            actions = { IconButton(onClick = {}) { Icon(Icons.Rounded.Add, "Crear playlist") } },
        )
        MessageState("Aún no hay playlists", "Podrás crear y ordenar tus propias colecciones.", action = "Crear playlist")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen() {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Favoritos", fontWeight = FontWeight.Bold) })
        MessageState("Aún no hay favoritos", "Las canciones que marques aparecerán aquí.")
    }
}
