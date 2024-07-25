package com.amory.musicapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amory.musicapp.managers.TrackManager
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.Track
import kotlinx.coroutines.launch

class DetailArtistViewModel : ViewModel() {
    private val _artist = MutableLiveData<Artists?>()
    val artist: LiveData<Artists?> get() = _artist

    private val _tracks = MutableLiveData<List<Track>?>()
    val tracks: LiveData<List<Track>?> get() = _tracks

    fun setArtist(artists: Artists) {
        _artist.value = artists
    }

    fun getTracks(artistId: String) {
        viewModelScope.launch {
            TrackManager.getTrackOfArtist(artistId, 1, 10) { tracks ->
                _tracks.value = tracks
            }
        }
    }
}