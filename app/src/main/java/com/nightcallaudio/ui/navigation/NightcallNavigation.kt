package com.nightcallaudio.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.nightcallaudio.domain.repository.PlaybackRepository
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.ui.collections.FavoritesScreen
import com.nightcallaudio.ui.collections.PlaylistsScreen
import com.nightcallaudio.ui.components.MiniPlayer
import com.nightcallaudio.ui.library.LibraryScreen
import com.nightcallaudio.ui.library.LibraryViewModel
import com.nightcallaudio.ui.player.PlayerScreen
import com.nightcallaudio.ui.player.QueueScreen
import com.nightcallaudio.ui.search.SearchScreen

private data class MainDestination(val route: String, val label: String, val icon: ImageVector)

private val mainDestinations = listOf(
    MainDestination("library", "Biblioteca", Icons.Rounded.LibraryMusic),
    MainDestination("search", "Buscar", Icons.Rounded.Search),
    MainDestination("playlists", "Playlists", Icons.AutoMirrored.Rounded.QueueMusic),
    MainDestination("favorites", "Favoritos", Icons.Rounded.Favorite),
)

@Composable
fun NightcallNavigation(
    libraryViewModel: LibraryViewModel,
    playbackRepository: PlaybackRepository,
    onPlayTracks: (List<Track>, Int) -> Unit,
) {
    val navController = rememberNavController()
    val libraryState by libraryViewModel.uiState.collectAsStateWithLifecycle()
    val query by libraryViewModel.query.collectAsStateWithLifecycle()
    val playbackState by playbackRepository.state.collectAsStateWithLifecycle()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showMainChrome = currentRoute in mainDestinations.map { it.route }

    Scaffold(
        bottomBar = {
            if (showMainChrome) {
                Column {
                    MiniPlayer(
                        state = playbackState,
                        onOpen = { navController.navigate("player") },
                        onPlayPause = { if (playbackState.isPlaying) playbackRepository.pause() else playbackRepository.play() },
                        onNext = playbackRepository::skipToNext,
                    )
                    NavigationBar {
                        mainDestinations.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(destination.icon, contentDescription = null) },
                                label = { Text(destination.label) },
                            )
                        }
                    }
                }
            }
        },
    ) { outerPadding ->
        NavHost(navController = navController, startDestination = "library", modifier = Modifier.padding(outerPadding)) {
            composable("library") {
                LibraryScreen(libraryState, { libraryViewModel.loadMusic(true) }) { index -> onPlayTracks(libraryState.tracks, index) }
            }
            composable("search") {
                SearchScreen(libraryState, query, libraryViewModel::updateQuery) { index -> onPlayTracks(libraryState.tracks, index) }
            }
            composable("playlists") { PlaylistsScreen() }
            composable("favorites") { FavoritesScreen() }
            composable("player") {
                PlayerScreen(
                    playbackState,
                    navController::navigateUp,
                    { if (playbackState.isPlaying) playbackRepository.pause() else playbackRepository.play() },
                    playbackRepository::skipToPrevious,
                    playbackRepository::skipToNext,
                    playbackRepository::seekTo,
                    { navController.navigate("queue") },
                )
            }
            composable("queue") {
                QueueScreen(playbackState, navController::navigateUp) { index -> onPlayTracks(playbackState.queue, index) }
            }
        }
    }
}
