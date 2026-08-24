package com.nightcallaudio.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nightcallaudio.data.mediastore.MediaStoreMusicRepository
import com.nightcallaudio.domain.model.Track
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

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MediaStoreMusicRepository(application)
    private val sourceTracks = MutableStateFlow<List<Track>>(emptyList())
    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        sourceTracks,
        loading,
        error,
        query,
    ) { tracks, isLoading, errorMessage, searchQuery ->
        val filtered = if (searchQuery.isBlank()) tracks else tracks.filter { track ->
            track.title.contains(searchQuery, ignoreCase = true) ||
                track.artist.contains(searchQuery, ignoreCase = true) ||
                track.album.contains(searchQuery, ignoreCase = true)
        }
        LibraryUiState(filtered, isLoading, errorMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    private var hasLoaded = false

    fun loadMusic(force: Boolean = false) {
        if (hasLoaded && !force) return
        viewModelScope.launch {
            loading.value = true
            error.value = null
            runCatching { repository.getTracks() }
                .onSuccess {
                    sourceTracks.value = it
                    hasLoaded = true
                }
                .onFailure { error.value = "No se ha podido leer la música del dispositivo." }
            loading.value = false
        }
    }

    fun updateQuery(value: String) {
        _query.value = value
    }
}
