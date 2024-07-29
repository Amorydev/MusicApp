package com.amory.musicapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amory.musicapp.managers.PlaylistManager
import com.amory.musicapp.managers.SearchManager
import com.amory.musicapp.managers.TrackManager
import com.amory.musicapp.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class AddItemInPlaylistViewModel : ViewModel() {
    private val _listTracks = MutableLiveData<List<Track?>>()
    val listTracks: LiveData<List<Track?>> get() = _listTracks

    private val _listTracksTemp = MutableLiveData<List<Track?>>()
    val listTracksTemp: LiveData<List<Track?>> get() = _listTracksTemp

    private val _isSuccess = MutableLiveData<Boolean?>()
    val isSuccess: LiveData<Boolean?> get() = _isSuccess

    fun searchTracks(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            SearchManager.getTrackSearch(query) {
                _listTracks.value = it
            }
        }
    }

    fun clearResults() {
        _listTracks.value = emptyList()
    }

    fun getPopularTrack() {
        viewModelScope.launch(Dispatchers.IO) {
            TrackManager.getTrack(1, 20) {
                _listTracksTemp.value = it
            }
        }
    }

    fun addItemInPlayList(id: String, requestBody: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            PlaylistManager.addItemInPlayList(id, requestBody) {
                _isSuccess.value = it
            }
        }
    }
}