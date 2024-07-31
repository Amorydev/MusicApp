package com.amory.musicapp.viewModel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.managers.AudioManger
import com.amory.musicapp.managers.PositionSongManger.setSongPosition
import com.amory.musicapp.model.Track
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class NowPlayingViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentTracks = MutableLiveData<Track>()
    val currentTracks: LiveData<Track> get() = _currentTracks

    private var _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private var _isLike = MutableLiveData<Boolean>()
    val isLike: LiveData<Boolean> get() = _isLike

    private val _backgroundGradient = MutableLiveData<Int?>()
    val backgroundGradient: LiveData<Int?> get() = _backgroundGradient

    fun init() {
        updateCurrentTrack()
        loadBackgroundGradient()
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

    fun updateLike(value: Boolean){
        _isLike.value = value
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

    fun toggleLike(){
        _isLike.value = !_isLike.value!!
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

    private fun loadBackgroundGradient() {
        PlayMusicActivity.listTracksSend?.let { track ->
            Glide.with(getApplication<Application>().applicationContext)
                .asBitmap()
                .load(track[PlayMusicActivity.positionTrackSend].thumbnail)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        Palette.from(resource).generate { palette ->
                            val dominantColor = palette?.dominantSwatch?.rgb ?: Color.TRANSPARENT
                            _backgroundGradient.value = dominantColor
                        }
                    }
                })
        }
    }
}
