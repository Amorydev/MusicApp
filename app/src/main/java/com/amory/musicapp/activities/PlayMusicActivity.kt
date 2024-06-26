package com.amory.musicapp.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.amory.musicapp.R
import com.amory.musicapp.databinding.ActivityPlayMusicBinding
import com.amory.musicapp.managers.UriAudioManger
import com.amory.musicapp.model.Track
import com.amory.musicapp.model.eventBus.EventPostListTrack
import com.amory.musicapp.service.MusicService
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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

        fun setSongPosition(increment: Boolean) {
            if (!repeat){
                if (increment) {
                    if (listTrack!!.size - 1 == positionTrack) {
                        positionTrack = 0
                    } else {
                        ++positionTrack
                    }
                } else {
                    if (0 == positionTrack) {
                        positionTrack = listTrack!!.size - 1
                    } else {
                        --positionTrack
                    }
                }
            }
        }

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
        initView()
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
    }

    private fun initView() {
        val position = positionTrack
        listTrack?.let { tracks ->
            tracks[position].let {
                binding.nameArtistTXT.text = it.artists.joinToString(", ") { artist -> artist.name }
                binding.songNameTXT.text = it.name
                Glide.with(binding.root).load(it.thumbnail).into(binding.imvTrack)
                binding.seekBar.progress = 0
            }
        } ?: run {
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
        binding.playImv.setOnClickListener {
            if (isPlayingMusic) {
                pauseMusic()
            } else {
                playMusic()
            }
        }
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
        // reset button
        musicService?.mediaPlayer?.setOnCompletionListener {
            binding.playImv.setImageResource(R.drawable.ic_play)
            binding.seekBar.progress = 0
            binding.startDurationTXT.text = formatTime(0)
            isPlayingMusic = false
        }
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
        if (increment) {
            setSongPosition(increment = false)
            Log.d("position", positionTrack.toString())
            initView()
            /*playMusic()*/
            getUriAudio(listTrack!![positionTrack])
        } else {
            setSongPosition(increment = true)
            Log.d("position", positionTrack.toString())
            initView()
            /*playMusic()*/
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
        UriAudioManger.getUriAudio(track) { uri ->
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
