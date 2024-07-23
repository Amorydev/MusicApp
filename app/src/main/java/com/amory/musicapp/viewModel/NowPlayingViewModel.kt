package com.amory.musicapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.managers.AudioManger
import com.amory.musicapp.managers.PositionSongManger.setSongPosition
import com.amory.musicapp.model.Track

class NowPlayingViewModel : ViewModel() {
    private val _currentTracks = MutableLiveData<Track>()
    val currentTracks: LiveData<Track> get() = _currentTracks

    private var _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    fun init() {
        updateCurrentTrack()
    }

    private fun updateCurrentTrack() {
        val position = PlayMusicActivity.positionTrackSend
        _currentTracks.value = PlayMusicActivity.listTracksSend?.get(position)
    }

    fun playOrPauseMusic() {
        if (_isPlaying.value == true) {
            pauseMusic()
        } else {
            playMusic()
        }
    }

    fun updateIsPlaying(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    private fun playMusic() {
        PlayMusicActivity.musicServiceSend?.mediaPlayer?.apply {
            start()
            PlayMusicActivity.musicServiceSend!!.showNotification(R.drawable.ic_pause_now)
        }
        _isPlaying.value = true
    }

    private fun pauseMusic() {
        PlayMusicActivity.musicServiceSend?.mediaPlayer?.apply {
            pause()
            PlayMusicActivity.musicServiceSend!!.showNotification(R.drawable.ic_play_now)
        }
        _isPlaying.value = false
    }

    fun nextSong() {
        setSongPosition(true)
        val listTracks = PlayMusicActivity.listTracksSend
        val positionTrack = PlayMusicActivity.positionTrackSend
        listTracks?.get(positionTrack)?.let {
            AudioManger.getUriAudio(it) { uriAudio ->
                uriAudio?.let { uri ->
                    PlayMusicActivity.musicServiceSend?.let { service ->
                        service.mediaPlayer?.apply {
                            reset()
                            setDataSource(uri)
                            prepareAsync()
                        }
                    }
                }
            }
        }
        PlayMusicActivity.musicServiceSend?.showNotification(R.drawable.ic_pause_now)
        updateCurrentTrack()
        playMusic()
    }
}
