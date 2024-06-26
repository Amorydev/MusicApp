package com.amory.musicapp.activities

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
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
import java.util.concurrent.TimeUnit

class PlayMusicActivity : AppCompatActivity(), ServiceConnection {
    private lateinit var binding: ActivityPlayMusicBinding
    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable

    companion object {
        var musicService: MusicService? = null
        var track: Track? = null
        var isPlaying: Boolean = false
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
        binding.imvBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getTrack() {
        track = intent.getSerializableExtra("track") as Track
        Log.d("track", track.toString())
    }

    private fun initView() {
        getTrack()
        for (i in 0 until track!!.artists.size) {
            binding.nameArtistTXT.text = track!!.artists[i].name
        }
        binding.songNameTXT.text = track!!.name
        Glide.with(binding.root).load(track!!.thumbnail).into(binding.imvTrack)
        binding.seekBar.progress = 0
    }

    private fun playTrack(uriAudio: String) {
        val audioUri = Uri.parse(uriAudio)
        musicService!!.mediaPlayer?.reset()
        musicService!!.mediaPlayer = MediaPlayer().apply {
            setDataSource(this@PlayMusicActivity, audioUri)
            setOnPreparedListener {
                binding.playImv.setImageResource(R.drawable.ic_pause)
                start()
                onStartAnim()
                binding.endDurationTXT.text = formatTime(duration.toLong())
                binding.seekBar.max = duration
            }
            prepareAsync()
        }

        binding.playImv.setOnClickListener {
            isPlaying = if (musicService!!.mediaPlayer?.isPlaying!!) {
                musicService!!.mediaPlayer?.pause()
                binding.playImv.setImageResource(R.drawable.ic_play)
                // Stop animation
                onStopAnim()
                false
            } else {
                musicService!!.mediaPlayer?.start()
                binding.playImv.setImageResource(R.drawable.ic_pause)
                // Start animation
                onStartAnim()
                true
            }
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService!!.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        // Update seekbar and duration
        runnable = Runnable {
            binding.seekBar.progress = musicService!!.mediaPlayer?.currentPosition!!
            binding.startDurationTXT.text =
                formatTime(musicService!!.mediaPlayer?.currentPosition!!.toLong())
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
        // reset button
        musicService!!.mediaPlayer?.setOnCompletionListener {
            binding.playImv.setImageResource(R.drawable.ic_play)
            binding.seekBar.progress = 0
            binding.startDurationTXT.text = formatTime(0)
            isPlaying = false
        }
    }

    private fun getUriAudio() {
        val service = RetrofitClient.retrofitInstance.create(APICallAudio::class.java)
        val audioIds = track?.audioFileIds?.joinToString(separator = ",")
        val callAudio = service.getAudioById(audioIds!!)
        callAudio.enqueue(object : Callback<AudioResponse> {
            override fun onResponse(call: Call<AudioResponse>, response: Response<AudioResponse>) {
                if (response.isSuccessful) {
                    val uriAudio = response.body()?.uris!![0]
                    Log.d("audioFile", uriAudio.toString())
                    playTrack(uriAudio)
                }
            }

            override fun onFailure(call: Call<AudioResponse>, t: Throwable) {}
        })
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
        if (musicService != null && musicService!!.mediaPlayer != null) {
            musicService!!.mediaPlayer?.release()
        }
        if (::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        getUriAudio()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }
}
