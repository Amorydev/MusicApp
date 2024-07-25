package com.amory.musicapp.viewModel

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amory.musicapp.managers.ArtistManager
import com.amory.musicapp.managers.TrackManager
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.Track
import kotlinx.coroutines.launch

class HomeViewModel:ViewModel() {
    private val _itemTrack = MutableLiveData<MutableList<Track>?>()
    val itemTrack: MutableLiveData<MutableList<Track>?> get() = _itemTrack

    private val _itemArtists = MutableLiveData<MutableList<Artists>?>()
    val itemArtists: MutableLiveData<MutableList<Artists>?> get() = _itemArtists

    private var isDataLoaded = false

    var trackListState: Parcelable? = null
    var artistListState: Parcelable? = null


    fun init() {
        if (!isDataLoaded) {
            getPopularTracks()
            getPopularArtists()
            isDataLoaded = true
        }

    }

     private fun getPopularTracks() {
       viewModelScope.launch {
           TrackManager.getTrack(1, 10) { track ->
               _itemTrack.postValue(track as MutableList<Track>?)
               Log.d("tracks", track.toString())
           }
       }
    }

    private fun getPopularArtists() {
        viewModelScope.launch {
            ArtistManager.getArtist(1, 5) { artists ->
                _itemArtists.postValue(artists)
            }
        }
    }
}