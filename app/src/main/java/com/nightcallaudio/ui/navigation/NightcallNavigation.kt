package com.nightcallaudio.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.nightcallaudio.R
import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.nightcallaudio.domain.repository.PlaybackRepository
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.ui.collections.FavoritesScreen
import com.nightcallaudio.ui.collections.PlaylistsScreen
import com.nightcallaudio.ui.collections.PlaylistDetailScreen
import com.nightcallaudio.ui.collections.CollectionsViewModel
import com.nightcallaudio.ui.components.MiniPlayer
import com.nightcallaudio.ui.library.LibraryScreen
import com.nightcallaudio.ui.library.LibraryViewModel
import com.nightcallaudio.ui.library.CollectionDetailScreen
import com.nightcallaudio.ui.player.PlayerScreen
import com.nightcallaudio.ui.player.QueueScreen
import com.nightcallaudio.ui.search.SearchScreen
import com.nightcallaudio.ui.settings.SettingsScreen

private data class MainDestination(val route: String, @StringRes val label: Int, val icon: ImageVector)

private val mainDestinations = listOf(
    MainDestination("library", R.string.navigation_library, Icons.Rounded.LibraryMusic),
    MainDestination("search", R.string.navigation_search, Icons.Rounded.Search),
    MainDestination("playlists", R.string.navigation_playlists, Icons.AutoMirrored.Rounded.QueueMusic),
    MainDestination("favorites", R.string.navigation_favorites, Icons.Rounded.Favorite),
)

@Composable
fun NightcallNavigation(
    libraryViewModel: LibraryViewModel,
    playbackRepository: PlaybackRepository,
    collectionsViewModel: CollectionsViewModel,
    onPlayTracks: (List<Track>, Int) -> Unit,
) {
    val navController = rememberNavController()
    val libraryState by libraryViewModel.uiState.collectAsStateWithLifecycle()
    val query by libraryViewModel.query.collectAsStateWithLifecycle()
    val playbackState by playbackRepository.state.collectAsStateWithLifecycle()
    val collectionsState by collectionsViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showMainChrome = currentRoute in mainDestinations.map { it.route } ||
        currentRoute == "artist/{artist}" || currentRoute == "album/{artist}/{album}" || currentRoute == "playlist/{playlistId}"

    LaunchedEffect(libraryState.tracks) {
        if (libraryState.tracks.isNotEmpty()) playbackRepository.restoreSession(libraryState.tracks)
    }
    LaunchedEffect(collectionsState.errorMessage) {
        collectionsState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            collectionsViewModel.clearError()
        }
    }
    LaunchedEffect(playbackState.errorMessage) {
        playbackState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                label = { Text(stringResource(destination.label)) },
                            )
                        }
                    }
                }
            }
        },
    ) { outerPadding ->
        NavHost(navController = navController, startDestination = "library", modifier = Modifier.padding(outerPadding)) {
            composable("library") {
                LibraryScreen(
                    libraryState,
                    { libraryViewModel.loadMusic(true) },
                    onPlayTracks,
                    playbackRepository::playNext,
                    playbackRepository::addToQueue,
                    { artist -> navController.navigate("artist/${Uri.encode(artist)}") },
                    { album, artist -> navController.navigate("album/${Uri.encode(artist)}/${Uri.encode(album)}") },
                    collectionsState.favoriteIds,
                    collectionsState.playlists,
                    { collectionsViewModel.toggleFavorite(it.id) },
                    { playlistId, track -> collectionsViewModel.addTrack(playlistId, track.id) },
                    { navController.navigate("settings") },
                )
            }
            composable("settings") { SettingsScreen(onBack = navController::navigateUp) }
            composable("artist/{artist}") { entry ->
                val artist = Uri.decode(entry.arguments?.getString("artist").orEmpty())
                val tracks = libraryState.tracks.filter { it.artist == artist }.sortedBy { it.title.lowercase() }
                CollectionDetailScreen(
                    title = artist,
                    subtitle = stringResource(R.string.artist_song_count, tracks.size),
                    tracks = tracks,
                    onBack = navController::navigateUp,
                    onPlayTracks = onPlayTracks,
                    onPlayNext = playbackRepository::playNext,
                    onAddToQueue = playbackRepository::addToQueue,
                    favoriteIds = collectionsState.favoriteIds,
                    playlists = collectionsState.playlists,
                    onToggleFavorite = { collectionsViewModel.toggleFavorite(it.id) },
                    onAddToPlaylist = { playlistId, track -> collectionsViewModel.addTrack(playlistId, track.id) },
                )
            }
            composable("album/{artist}/{album}") { entry ->
                val artist = Uri.decode(entry.arguments?.getString("artist").orEmpty())
                val album = Uri.decode(entry.arguments?.getString("album").orEmpty())
                val tracks = libraryState.tracks
                    .filter { it.artist == artist && it.album == album }
                    .sortedWith(compareBy<Track>({ it.discNumber ?: 1 }, { it.trackNumber ?: Int.MAX_VALUE }, { it.title.lowercase() }))
                CollectionDetailScreen(
                    title = album,
                    subtitle = stringResource(R.string.album_song_count, artist, tracks.size),
                    tracks = tracks,
                    onBack = navController::navigateUp,
                    onPlayTracks = onPlayTracks,
                    onPlayNext = playbackRepository::playNext,
                    onAddToQueue = playbackRepository::addToQueue,
                    favoriteIds = collectionsState.favoriteIds,
                    playlists = collectionsState.playlists,
                    onToggleFavorite = { collectionsViewModel.toggleFavorite(it.id) },
                    onAddToPlaylist = { playlistId, track -> collectionsViewModel.addTrack(playlistId, track.id) },
                )
            }
            composable("search") {
                SearchScreen(
                    libraryState,
                    query,
                    libraryViewModel::updateQuery,
                    { index -> onPlayTracks(libraryState.searchResults, index) },
                    playbackRepository::playNext,
                    playbackRepository::addToQueue,
                    collectionsState.favoriteIds,
                    collectionsState.playlists,
                    { collectionsViewModel.toggleFavorite(it.id) },
                    { playlistId, track -> collectionsViewModel.addTrack(playlistId, track.id) },
                )
            }
            composable("playlists") {
                PlaylistsScreen(
                    collectionsState,
                    collectionsViewModel::create,
                    collectionsViewModel::rename,
                    collectionsViewModel::delete,
                    { navController.navigate("playlist/$it") },
                )
            }
            composable("playlist/{playlistId}") { entry ->
                val playlistId = entry.arguments?.getString("playlistId")?.toLongOrNull() ?: -1L
                PlaylistDetailScreen(
                    collectionsState.playlists.firstOrNull { it.id == playlistId },
                    navController::navigateUp,
                    onPlayTracks,
                    { collectionsViewModel.removeTrack(playlistId, it) },
                    { from, to -> collectionsViewModel.moveTrack(playlistId, from, to) },
                )
            }
            composable("favorites") {
                val favoriteTracks = libraryState.tracks.filter { it.id in collectionsState.favoriteIds }
                FavoritesScreen(
                    favoriteTracks,
                    onPlayTracks,
                    collectionsViewModel::toggleFavorite,
                    playbackRepository::playNext,
                    playbackRepository::addToQueue,
                )
            }
            composable("player") {
                PlayerScreen(
                    playbackState,
                    navController::navigateUp,
                    { if (playbackState.isPlaying) playbackRepository.pause() else playbackRepository.play() },
                    playbackRepository::skipToPrevious,
                    playbackRepository::skipToNext,
                    playbackRepository::seekBack,
                    playbackRepository::seekForward,
                    { playbackRepository.setShuffleEnabled(!playbackState.shuffleEnabled) },
                    {
                        playbackRepository.setRepeatMode(
                            when (playbackState.repeatMode) {
                                com.nightcallaudio.domain.model.RepeatMode.OFF -> com.nightcallaudio.domain.model.RepeatMode.ALL
                                com.nightcallaudio.domain.model.RepeatMode.ALL -> com.nightcallaudio.domain.model.RepeatMode.ONE
                                com.nightcallaudio.domain.model.RepeatMode.ONE -> com.nightcallaudio.domain.model.RepeatMode.OFF
                            },
                        )
                    },
                    playbackRepository::seekTo,
                    { navController.navigate("queue") },
                    playbackState.currentTrack?.id in collectionsState.favoriteIds,
                    { playbackState.currentTrack?.let { collectionsViewModel.toggleFavorite(it.id) } },
                )
            }
            composable("queue") {
                QueueScreen(
                    playbackState,
                    navController::navigateUp,
                    playbackRepository::skipTo,
                    playbackRepository::removeFromQueue,
                    playbackRepository::moveQueueItem,
                )
            }
        }
    }
}
