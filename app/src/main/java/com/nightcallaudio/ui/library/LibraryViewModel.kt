package com.nightcallaudio.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.nightcallaudio.domain.model.MusicLibrary
import com.nightcallaudio.domain.model.Track
import com.nightcallaudio.domain.usecase.GetMusicLibraryUseCase
import com.nightcallaudio.domain.usecase.SearchTracksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class LibraryViewModel(
    private val getMusicLibrary: GetMusicLibraryUseCase,
    private val searchTracks: SearchTracksUseCase,
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
        LibraryUiState(filtered, isLoading, errorMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    private var hasLoaded = false

    fun loadMusic(force: Boolean = false) {
        if (hasLoaded && !force) return
        viewModelScope.launch {
            loading.value = true
            error.value = null
            runCatching { getMusicLibrary() }
                .onSuccess {
                    sourceLibrary.value = it
                    hasLoaded = true
                }
                .onFailure { error.value = "No se ha podido leer la música del dispositivo." }
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
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                require(modelClass.isAssignableFrom(LibraryViewModel::class.java))
                return LibraryViewModel(getMusicLibrary, searchTracks) as T
            }
        }
    }
}
