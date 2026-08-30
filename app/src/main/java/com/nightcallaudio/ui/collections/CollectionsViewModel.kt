package com.nightcallaudio.ui.collections

import androidx.lifecycle.ViewModel
import com.nightcallaudio.ui.settings.DynamicMessages
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.nightcallaudio.domain.model.Playlist
import com.nightcallaudio.domain.repository.FavoritesRepository
import com.nightcallaudio.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionsUiState(
    val playlists: List<Playlist> = emptyList(),
    val favoriteIds: Set<Long> = emptySet(),
    val errorMessage: String? = null,
)

class CollectionsViewModel(
    private val playlists: PlaylistRepository,
    private val favorites: FavoritesRepository,
) : ViewModel() {
    private val error = MutableStateFlow<String?>(null)

    val state: StateFlow<CollectionsUiState> = combine(
        playlists.observePlaylists(),
        favorites.observeFavoriteIds(),
        error,
    ) { playlistItems, favoriteIds, message ->
        CollectionsUiState(playlistItems, favoriteIds, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionsUiState())

    fun create(name: String) = execute { playlists.create(name) }
    fun rename(id: Long, name: String) = execute { playlists.rename(id, name) }
    fun delete(id: Long) = execute { playlists.delete(id) }
    fun addTrack(playlistId: Long, trackId: Long) = execute {
        if (!playlists.addTrack(playlistId, trackId)) error.value = DynamicMessages.duplicatePlaylistTrack
    }
    fun removeTrack(playlistId: Long, trackId: Long) = execute { playlists.removeTrack(playlistId, trackId) }
    fun moveTrack(playlistId: Long, from: Int, to: Int) = execute { playlists.moveTrack(playlistId, from, to) }
    fun toggleFavorite(trackId: Long) = execute {
        favorites.setFavorite(trackId, trackId !in state.value.favoriteIds)
    }
    fun clearError() { error.value = null }

    private fun execute(block: suspend () -> Unit) {
        viewModelScope.launch {
            error.value = null
            runCatching { block() }.onFailure {
                error.value = it.message ?: DynamicMessages.operationFailed
            }
        }
    }

    companion object {
        fun factory(
            playlists: PlaylistRepository,
            favorites: FavoritesRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                require(modelClass.isAssignableFrom(CollectionsViewModel::class.java))
                return CollectionsViewModel(playlists, favorites) as T
            }
        }
    }
}
