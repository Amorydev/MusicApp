package com.amory.musicapp.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.amory.musicapp.managers.AudioManger
import com.amory.musicapp.managers.AudioManger.getUriAudio
import com.amory.musicapp.model.Track
import com.amory.musicapp.model.eventBus.EventPostListTrack
import com.amory.musicapp.service.MusicService
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PlayMusicViewModel(application: Application) : AndroidViewModel(application) {
    private val _track = MutableLiveData<Track?>()
    val track: LiveData<Track?> get() = _track

    private val _isPlaying = MutableLiveData<Boolean?>()
    val isPlaying: LiveData<Boolean?> get() = _isPlaying

    private val _repeat = MutableLiveData<Boolean?>()
    val repeat: LiveData<Boolean?> get() = _repeat

    private val _shuffle = MutableLiveData<Boolean?>()
    val shuffle: LiveData<Boolean?> get() = _shuffle

    private val _backgroundGradient = MutableLiveData<GradientDrawable?>()
    val backgroundGradient: LiveData<GradientDrawable?> get() = _backgroundGradient

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> get() = _currentPosition

    private val _duration = MutableLiveData<Int>()
    val duration: LiveData<Int> get() = _duration

    @SuppressLint("StaticFieldLeak")
    var musicService: MusicService? = null
    private var listTracks: List<Track>? = null
    private var positionTrack: Int = 0
    private val handler = Handler(Looper.getMainLooper())

    init {
        _shuffle.value = false
        _repeat.value = false
        EventBus.getDefault().register(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventPostListTrack) {
        listTracks = event.listTrack
    }

    private fun updateTrack() {
        _track.value = listTracks!![positionTrack]
        loadBackgroundGradient()
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    fun toggleShuffle() {
        _shuffle.value = !_shuffle.value!!
    }

    fun toggleRepeat() {
        _repeat.value = !_repeat.value!!
    }

    fun nextTrack() {
        positionTrack = (positionTrack + 1) % (listTracks?.size ?: 1)
        updateTrack()
        playTrack()
    }

    fun previousTrack() {
        positionTrack = (positionTrack - 1 + (listTracks?.size ?: 1)) % (listTracks?.size ?: 1)
        updateTrack()
        playTrack()
    }

    fun setPositionTrack(position: Int) {
        positionTrack = position
        updateTrack()
        playTrack()
    }

    private fun loadBackgroundGradient() {
        listTracks?.let { track ->
            Glide.with(getApplication<Application>().applicationContext)
                .asBitmap()
                .load(track[positionTrack].thumbnail)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        Palette.from(resource).generate { palette ->
                            val dominantColor = palette?.dominantSwatch?.rgb ?: Color.TRANSPARENT
                            val gradientDrawable = GradientDrawable().apply {
                                shape = GradientDrawable.RECTANGLE
                                colors = intArrayOf(dominantColor, 0xFF434343.toInt())
                            }
                            _backgroundGradient.value = gradientDrawable
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }

    private fun playTrack() {
        val track = listTracks!![positionTrack] ?: return
        viewModelScope.launch {
            getUriAudio(track) { uri ->
                val uriAudio = Uri.parse(uri)
                createMediaPlayer(uriAudio)
            }
        }
        musicService!!.mediaPlayer!!.setOnCompletionListener {
            _isPlaying.value = false
            _currentPosition.value = 0
        }
    }

    private fun createMediaPlayer(uriAudio: Uri?) {
        try {
            musicService?.mediaPlayer = MediaPlayer().apply {
                reset()
                setDataSource(getApplication<Application>().applicationContext, uriAudio!!)
                setOnPreparedListener {
                    _isPlaying.value = true
                    start()
                    _duration.value = duration
                    startSeekBarUpdate()
                }
                prepareAsync()
            }
        } catch (ex: Exception) {
            // Handle exception
            return
        }
    }

    fun playMusic() {
        musicService?.mediaPlayer?.start()
        _isPlaying.value = true
        startSeekBarUpdate()
    }

    fun pauseMusic() {
        musicService?.mediaPlayer?.pause()
        _isPlaying.value = false
        stopSeekBarUpdate()
    }

    private fun startSeekBarUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                musicService?.mediaPlayer?.let {
                    _currentPosition.value = it.currentPosition
                }
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun stopSeekBarUpdate() {
        handler.removeCallbacksAndMessages(null)
    }

}