package com.amory.musicapp.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.amory.musicapp.R
import com.amory.musicapp.databinding.ActivityPlayMusicBinding
import com.amory.musicapp.managers.PositionSongManger.setSongPosition
import com.amory.musicapp.managers.AudioManger
import com.amory.musicapp.managers.AudioManger.getUriAudio
import com.amory.musicapp.model.Track
import com.amory.musicapp.model.eventBus.EventPostListTrack
import com.amory.musicapp.service.MusicService
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

class PlayMusicActivity : AppCompatActivity(), ServiceConnection {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var shuffle: Boolean = false

    companion object {
        var musicService: MusicService? = null
        var listTrack: MutableList<Track>? = null
        var positionTrack: Int = 0
        var isPlayingMusic: Boolean = false
        var repeat: Boolean = false

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayMusicBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // For starting service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
        getPositionTrack()
        onClickBack()
        onClickShuffle()
        shuffleTracks()
        onClickRepeat()

        binding.nextBtn.setOnClickListener {
            nextOrPreviousMusic(increment = false)
        }
        binding.previousBtn.setOnClickListener {
            nextOrPreviousMusic(increment = true)
        }
    }


    private fun onClickRepeat() {
        binding.repeatBtn.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatBtn.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
            } else {
                repeat = false
                binding.repeatBtn.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            }
        }
    }

    private fun onClickShuffle() {
        binding.shuffleBtn.setOnClickListener {
            shuffle = !shuffle
            if (shuffle) {
                binding.shuffleBtn.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
            } else {
                binding.shuffleBtn.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            }
        }
    }

    private fun shuffleTracks() {
        if (shuffle) {
            listTrack!!.shuffle()
        }
    }

    private fun onClickBack() {
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getPositionTrack() {
        positionTrack = intent.getIntExtra("positionTrack", 0)
        val currentPosition = intent.getIntExtra("currentPosition", 0)
        when (intent.getStringExtra("class")) {
            "NowPlaying" -> {
                initView()
                binding.startDurationTXT.text =
                    formatTime(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.endDurationTXT.text =
                    formatTime(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBar.max = musicService!!.mediaPlayer!!.duration
                musicService?.mediaPlayer?.seekTo(currentPosition)
                setUpSeekBar()
                if (isPlayingMusic) {
                    binding.playImv.setImageResource(R.drawable.ic_pause_now)
                    playMusic()
                } else {
                    binding.playImv.setImageResource(R.drawable.ic_play_now)
                    pauseMusic()
                }
                onClickPlay()
            }
        }
    }

    private fun initView() {
        val position = positionTrack
        if (listTrack != null && listTrack!!.isNotEmpty()) {
            listTrack?.let { tracks ->
                tracks[position].let {
                    binding.nameArtistTXT.text =
                        it.artists.joinToString(", ") { artist -> artist.name }
                    binding.songNameTXT.text = it.name
                    Glide.with(binding.root).load(it.thumbnail).into(binding.imvTrack)
                    binding.seekBar.progress = 0
                    setLayout()
                }
            }
        }else {
            Log.e("PlayMusicActivity", "listTrack is null")
        }
        if (repeat) {
            binding.repeatBtn.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
        } else {
            binding.repeatBtn.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
        }
    }

    private fun setLayout() {
        listTrack?.let { tracks ->
            Glide.with(baseContext)
                .asBitmap()
                .load(tracks[positionTrack].thumbnail)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        Palette.from(resource).generate { palette ->
                            val dominantColor = palette?.dominantSwatch?.rgb ?: Color.TRANSPARENT

                            val gradientDrawable = GradientDrawable()
                            gradientDrawable.shape  = GradientDrawable.RECTANGLE
                            gradientDrawable.colors = intArrayOf( dominantColor,0xFF434343.toInt()) // Đổi endColor thành màu đỏ

                            binding.root.background = gradientDrawable

                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } ?: run {
            Log.e("PlayMusicActivity", "listTrack is null")
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()

        // Load notification icon asynchronously
        Executors.newSingleThreadExecutor().execute {
            musicService?.showNotification(R.drawable.ic_pause_now)
            runOnUiThread {
                getUriAudio(listTrack!![positionTrack])
            }
        }
    }

    private fun playTrack(uriAudio: String) {
        val audioUri = Uri.parse(uriAudio)
        createMediaPlayer(audioUri)
        onClickPlay()
        setUpSeekBar()
        // reset button
        musicService?.mediaPlayer?.setOnCompletionListener {
            binding.playImv.setImageResource(R.drawable.ic_play)
            binding.seekBar.progress = 0
            binding.startDurationTXT.text = formatTime(0)
            isPlayingMusic = false
        }
    }

    private fun onClickPlay() {
        binding.playImv.setOnClickListener {
            if (isPlayingMusic) {
                pauseMusic()
            } else {
                playMusic()
            }
        }
    }

    private fun setUpSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService?.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        // Update seekbar and duration
        runnable = Runnable {
            musicService?.mediaPlayer?.let {
                binding.seekBar.progress = it.currentPosition
                binding.startDurationTXT.text = formatTime(it.currentPosition.toLong())
            }
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

    private fun createMediaPlayer(uriAudio: Uri) {
        try {
            musicService?.mediaPlayer = MediaPlayer().apply {
                reset()
                setDataSource(this@PlayMusicActivity, uriAudio)
                setOnPreparedListener {
                    isPlayingMusic = true
                    binding.playImv.setImageResource(R.drawable.ic_pause)
                    start()
                    onStartAnim()
                    binding.endDurationTXT.text = formatTime(duration.toLong())
                    binding.seekBar.max = duration
                }
                prepareAsync()
            }
        } catch (ex: Exception) {
            return
        }
    }

    private fun nextOrPreviousMusic(increment: Boolean) {
        musicService!!.mediaPlayer!!.reset()
        if (increment) {
            setSongPosition(increment = false)
            Log.d("position", positionTrack.toString())
            initView()
            getUriAudio(listTrack!![positionTrack])
        } else {
            setSongPosition(increment = true)
            Log.d("position", positionTrack.toString())
            initView()
            getUriAudio(listTrack!![positionTrack])
        }
    }

    private fun pauseMusic() {
        musicService?.mediaPlayer?.pause()
        binding.playImv.setImageResource(R.drawable.ic_play)
        // Stop animation
        onStopAnim()
        musicService?.showNotification(R.drawable.ic_play_now)
        isPlayingMusic = false
    }

    private fun playMusic() {
        musicService?.mediaPlayer?.start()
        binding.playImv.setImageResource(R.drawable.ic_pause)
        // Start animation
        musicService?.mediaPlayer?.let {
            if (it.isPlaying) {
                onStartAnim()
            }
        }
        musicService?.showNotification(R.drawable.ic_pause_now)
        isPlayingMusic = true
    }

    private fun getUriAudio(track: Track) {
        AudioManger.getUriAudio(track) { uri ->
            uri?.let { playTrack(it) }
        }
    }

    private fun onStartAnim() {
        val runnable = object : Runnable {
            override fun run() {
                binding.imvTrack.animate().rotationBy(360F).withEndAction(this).setDuration(10000)
                    .setInterpolator(LinearInterpolator()).start()
            }
        }
        binding.imvTrack.animate().rotationBy(360F).withEndAction(runnable).setDuration(10000)
            .setInterpolator(LinearInterpolator()).start()
    }

    private fun onStopAnim() {
        binding.imvTrack.animate().cancel()
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
        unbindService(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun eventListTrack(event: EventPostListTrack) {
        listTrack = event.listTrack
        initView()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}