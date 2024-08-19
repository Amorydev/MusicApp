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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel:ViewModel() {
    private val _itemTrack = MutableLiveData<MutableList<Track>?>()
    val itemTrack: MutableLiveData<MutableList<Track>?> get() = _itemTrack

    private val _itemArtists = MutableLiveData<MutableList<Artists>?>()
    val itemArtists: MutableLiveData<MutableList<Artists>?> get() = _itemArtists


    var trackListState: Parcelable? = null
    var artistListState: Parcelable? = null


    fun init() {
        if (_itemTrack.value == null && _itemArtists.value == null) {
            getPopularTracks()
            getPopularArtists()
        }

    }

     private fun getPopularTracks() {
       viewModelScope.launch {
          try {
              val tracks = fetchDataPopularTracks()
              _itemTrack.postValue(tracks as MutableList<Track>)
          }catch (ex: Exception){
              Log.d("Error", ex.toString())
          }
       }
    }
    private suspend fun fetchDataPopularTracks(){
       return withContext(Dispatchers.IO){
           TrackManager.getTrack(1, 10) { track ->
               track!!
           }
       }
    }

    private fun getPopularArtists() {
        viewModelScope.launch {
            try {
                val artists = fetchDataPopularArtist()
                _itemArtists.postValue(artists as MutableList<Artists>)
            }catch (ex: Exception){
                Log.d("Error", ex.toString())
            }
        }
    }

    private suspend fun fetchDataPopularArtist() {
        return withContext(Dispatchers.IO) {
            ArtistManager.getArtist(1, 5) { artists ->
                artists!!
            }
        }
    }
}