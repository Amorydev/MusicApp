package com.amory.musicapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amory.musicapp.managers.SearchManager
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.Track
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _artists = MutableLiveData<List<Artists>?>()
    val artists: LiveData<List<Artists>?> get() = _artists

    private val _tracks = MutableLiveData<List<Track>?>()
    val tracks: LiveData<List<Track>?> get() = _tracks

    fun searchArtist(search: String) {
        viewModelScope.launch {
            SearchManager.getArtistSearch(search) { artist ->
                _artists.value = artist
            }
        }
    }

    fun searchTrack(search: String) {
        SearchManager.getTrackSearch(search) { track ->
            _tracks.value = track
        }
    }

    fun clearResults() {
        _artists.value = emptyList()
        _tracks.value = emptyList()
    }
}