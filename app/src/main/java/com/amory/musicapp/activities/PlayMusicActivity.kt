package com.amory.musicapp.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
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
import com.amory.musicapp.R
import com.amory.musicapp.databinding.ActivityPlayMusicBinding
import com.amory.musicapp.model.AudioResponse
import com.amory.musicapp.model.Track
import com.amory.musicapp.retrofit.APICallAudio
import com.amory.musicapp.retrofit.RetrofitClient
import com.amory.musicapp.service.MusicService
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PlayMusicActivity : AppCompatActivity(), ServiceConnection {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    companion object {
        var musicService: MusicService? = null
        var track: Track? = null
        var isPlayingMusic: Boolean = false

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
        initView()
        onClickBack()
    }

    private fun onClickBack() {
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getTrack() {
        track = intent.getSerializableExtra("track") as Track?
        Log.d("track", track.toString())
    }

    private fun initView() {
        getTrack()
        track?.let {
            binding.nameArtistTXT.text = it.artists.joinToString(", ") { artist -> artist.name }
            binding.songNameTXT.text = it.name
            Glide.with(binding.root).load(it.thumbnail).into(binding.imvTrack)
            binding.seekBar.progress = 0
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()

        // Load notification icon asynchronously
        Executors.newSingleThreadExecutor().execute {
            musicService?.showNotification(R.drawable.ic_pause_now)
            runOnUiThread {
                getUriAudio()
            }
        }
    }

    private fun playTrack(uriAudio: String) {
        val audioUri = Uri.parse(uriAudio)
        try {
            musicService?.mediaPlayer = MediaPlayer().apply {
                reset()
                setDataSource(this@PlayMusicActivity, audioUri)
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

    private fun getUriAudio() {
        val service = RetrofitClient.retrofitInstance.create(APICallAudio::class.java)
        val audioIds = track?.audioFileIds?.joinToString(separator = ",")
        audioIds?.let {
            val callAudio = service.getAudioById(it)
            callAudio.enqueue(object : Callback<AudioResponse> {
                override fun onResponse(
                    call: Call<AudioResponse>,
                    response: Response<AudioResponse>
                ) {
                    if (response.isSuccessful) {
                        val uriAudio = response.body()?.uris?.get(0)
                        Log.d("audioFile", uriAudio.toString())
                        uriAudio?.let { it1 -> playTrack(it1) }
                    }
                }

                override fun onFailure(call: Call<AudioResponse>, t: Throwable) {}
            })
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
        musicService?.mediaPlayer?.release()
        if (::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
        unbindService(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }
}
