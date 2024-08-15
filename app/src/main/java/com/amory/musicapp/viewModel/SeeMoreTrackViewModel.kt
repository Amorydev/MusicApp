package com.amory.musicapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amory.musicapp.managers.TrackManager
import com.amory.musicapp.model.Track
import kotlinx.coroutines.launch

class SeeMoreTrackViewModel : ViewModel() {
    private val _listTrack = MutableLiveData<List<Track>>()
    val listTrack: LiveData<List<Track>> get() = _listTrack

    fun getTrack(page: Int, size: Int) {
        viewModelScope.launch {
            TrackManager.getTrack(page, size) {
                Log.d("listTrackSeeMore", it.toString())
                _listTrack.value = it
            }
        }
    }
}