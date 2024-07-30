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
import kotlinx.coroutines.Dispatchers
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

    private val _musicService = MutableLiveData<MusicService?>()
    val musicService: LiveData<MusicService?> get() = _musicService

    private val _listTrackResponse = MutableLiveData<List<Track>?>()
    val listTrackResponse: LiveData<List<Track>?> get() = _listTrackResponse

    private val _positionTrackResponse = MutableLiveData<Int?>()
    val positionTrackResponse: LiveData<Int?> get() = _positionTrackResponse

    private val _uri = MutableLiveData<Uri?>()
    val uri: LiveData<Uri?> get() = _uri

    private var isTrackChangedFromHome: Boolean = false

    @SuppressLint("StaticFieldLeak")
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
        _listTrackResponse.value = listTracks
    }

    fun setTrackChangedFromHome(isChanged: Boolean) {
        isTrackChangedFromHome = isChanged
    }

    private fun updateTrack() {
        viewModelScope.launch {
            _track.value = listTracks!![positionTrack]
            _positionTrackResponse.value = positionTrack
            Log.d("PlayMusicViewModel", _positionTrackResponse.value.toString())
            loadBackgroundGradient()
        }
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

    fun updateIsPlaying(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun setMusicService(service: MusicService) {
        _musicService.value = service
    }


    fun playMusic() {
        Log.d("PlayMusicViewModel", "playMusic called, isPlaying: ${_isPlaying.value}")
        _musicService.value?.mediaPlayer?.let {
            _isPlaying.value = true
            startSeekBarUpdate()
            it.start()
        }
    }

    fun pauseMusic() {
        Log.d("PlayMusicViewModel", "pauseMusic called, isPlaying: ${_isPlaying.value}")
        _musicService.value?.mediaPlayer?.let {
            _isPlaying.value = false
            stopSeekBarUpdate()
            it.pause()
        }
    }

    fun playMusicIfNotPlaying() {
        if (_isPlaying.value == false) {
            playMusic()
        }
    }

    private fun playTrack() {
        if (_musicService.value?.mediaPlayer?.isPlaying == true && !isTrackChangedFromHome) {
            return
        }
        isTrackChangedFromHome = false  // Reset flag after use
        viewModelScope.launch(Dispatchers.IO) {
            val track = listTracks!![positionTrack] ?: return@launch
            getUriAudio(track) { uri ->
                val uriAudio = Uri.parse(uri)
                _uri.value = uriAudio
                createMediaPlayer(uriAudio)
            }
        }
    }

    fun updateCurrentPosition(position: Int) {
        _currentPosition.value = position
    }

    private fun createMediaPlayer(uriAudio: Uri?) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                _musicService.value?.mediaPlayer?.reset()
                _musicService.value?.mediaPlayer = MediaPlayer().apply {
                    reset()
                    setDataSource(getApplication<Application>().applicationContext, uriAudio!!)
                    setOnPreparedListener {
                        _isPlaying.value = true
                        start()
                        _duration.value = duration
                        startSeekBarUpdate()
                    }
                    setOnCompletionListener {
                        nextTrack()  // Chuyển sang bài hát tiếp theo khi hoàn thành bài hát hiện tại
                    }
                    prepareAsync()
                }
            } catch (ex: Exception) {
                // Handle exception
                return@launch
            }
        }
    }

    fun startSeekBarUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                _musicService.value?.mediaPlayer?.let {
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