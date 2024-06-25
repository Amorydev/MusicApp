package com.amory.musicapp.viewModel

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amory.musicapp.model.Track

class SharedViewModel: ViewModel() {
    private val _currentTrack = MutableLiveData<Track>()
    val currentTrack: LiveData<Track> get() = _currentTrack

    private val _mediaPlayer = MutableLiveData<MediaPlayer?>()
    val mediaPlayer: LiveData<MediaPlayer?> = _mediaPlayer

    fun setTrack(track: Track){
        _currentTrack.value = track
    }

    fun setMediaPlayer(mediaPlayer: MediaPlayer) {
        _mediaPlayer.value = mediaPlayer
    }
}