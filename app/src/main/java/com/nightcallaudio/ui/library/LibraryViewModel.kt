package com.nightcallaudio.ui.library

import androidx.lifecycle.ViewModel
import com.nightcallaudio.ui.settings.DynamicMessages
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.nightcallaudio.domain.model.MusicLibrary
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.model.Artist
import com.nightcallaudio.domain.model.Album
import com.nightcallaudio.domain.model.Genre
import com.nightcallaudio.domain.model.MusicFolder
import com.nightcallaudio.domain.usecase.GetMusicLibraryUseCase
import com.nightcallaudio.domain.usecase.SearchTracksUseCase
import com.nightcallaudio.domain.usecase.CleanupMissingReferencesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.launchIn

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val folders: List<MusicFolder> = emptyList(),
    val searchResults: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class LibraryViewModel(
    private val getMusicLibrary: GetMusicLibraryUseCase,
    private val searchTracks: SearchTracksUseCase,
    private val cleanupMissingReferences: CleanupMissingReferencesUseCase,
) : ViewModel() {
    private val sourceLibrary = MutableStateFlow<MusicLibrary?>(null)
    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        sourceLibrary,
        loading,
        error,
        query,
    ) { library, isLoading, errorMessage, searchQuery ->
        val filtered = searchTracks(library?.tracks.orEmpty(), searchQuery)
        LibraryUiState(
            tracks = library?.tracks.orEmpty(),
            artists = library?.artists.orEmpty(),
            albums = library?.albums.orEmpty(),
            genres = library?.genres.orEmpty(),
            folders = library?.folders.orEmpty(),
            searchResults = filtered,
            isLoading = isLoading,
            errorMessage = errorMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    private var observationJob: Job? = null

    fun loadMusic(force: Boolean = false) {
        if (observationJob?.isActive != true) {
            observationJob = getMusicLibrary.observe()
                .onStart {
                    loading.value = true
                    error.value = null
                }
                .onEach { library ->
                    sourceLibrary.value = library
                    cleanupMissingReferences(library.tracks.mapTo(mutableSetOf()) { it.id })
                    loading.value = false
                    error.value = null
                }
                .catch {
                    loading.value = false
                    error.value = DynamicMessages.libraryReadFailed
                }
                .launchIn(viewModelScope)
        }
        if (!force) return
        viewModelScope.launch {
            loading.value = true
            error.value = null
            runCatching { getMusicLibrary() }
                .onSuccess {
                    sourceLibrary.value = it
                }
                .onFailure { error.value = DynamicMessages.libraryReadFailed }
            loading.value = false
        }
    }

    fun updateQuery(value: String) {
        _query.value = value
    }

    companion object {
        fun factory(
            getMusicLibrary: GetMusicLibraryUseCase,
            searchTracks: SearchTracksUseCase,
            cleanupMissingReferences: CleanupMissingReferencesUseCase,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                require(modelClass.isAssignableFrom(LibraryViewModel::class.java))
                return LibraryViewModel(getMusicLibrary, searchTracks, cleanupMissingReferences) as T
            }
        }
    }
}
