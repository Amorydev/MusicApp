package com.amory.musicapp.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.amory.musicapp.R
import com.amory.musicapp.databinding.ActivityPlayMusicBinding
import com.amory.musicapp.fragment.NowPlayingFragment.Companion.binding
import com.amory.musicapp.model.Track

import com.amory.musicapp.service.MusicService
import com.amory.musicapp.viewModel.PlayMusicViewModel
import com.bumptech.glide.Glide

import java.util.concurrent.Executors

class PlayMusicActivity : AppCompatActivity(), ServiceConnection {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayMusicBinding
        var isPlayingSend: Boolean = true
        var musicServiceSend: MusicService? = null
        var listTracksSend: List<Track>? = null
        var positionTrackSend: Int = 0
    }

    private var positionTrack: Int = 0

    private val viewModel: PlayMusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            updateIsPlayingReceiver,
            IntentFilter("UPDATE_IS_PLAYING")
        )

        positionTrack = intent.getIntExtra("positionTrack", 0)

        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)

        initView()
    }

    private fun initView() {
        Log.d("class",intent.getStringExtra("class").toString())
        when (intent.getStringExtra("class")) {
            "HomeFragment" -> {
                setupObservers()
                setupClickListeners()
            }
            "NowPlayingFragment" -> {
                val currentPosition = intent.getIntExtra("currentPosition", 0)
                viewModel.updateCurrentPosition(currentPosition)
               /* setupObservers()*/
                viewModel.musicService.observe(this, Observer { musicService ->
                    musicService?.mediaPlayer?.seekTo(currentPosition)
                    binding.seekBar.progress = currentPosition
                    binding.seekBar.max = musicService?.mediaPlayer?.duration!!
                })

                viewModel.track.observe(this, Observer { track ->
                    track?.let {
                        binding.nameArtistTXT.text = it.artists.joinToString(", ") { artist -> artist.name }
                        binding.songNameTXT.text = it.name
                        Glide.with(binding.root).load(it.thumbnail).into(binding.imvTrack)
                    }
                })
                viewModel.shuffle.observe(this, Observer { shuffle ->
                    shuffle?.let {
                        binding.shuffleBtn.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(this, if (it) R.color.primary else R.color.white)
                        )
                    }
                })

                viewModel.repeat.observe(this, Observer { repeat ->
                    repeat?.let {
                        binding.repeatBtn.backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(this, if (it) R.color.primary else R.color.white)
                        )
                    }
                })

                viewModel.duration.observe(this, Observer { duration ->
                    duration?.let {
                        binding.seekBar.max = it
                        binding.endDurationTXT.text = formatTime(it.toLong())
                    }
                })

                viewModel.backgroundGradient.observe(this, Observer { gradientDrawable ->
                    gradientDrawable?.let {
                        binding.root.background = it
                    }
                })

                viewModel.isPlaying.observe(this, Observer { isPlaying ->
                    if (isPlaying == true){
                        binding.playImv.setImageResource(R.drawable.ic_pause_now)
                        onStartAnim()
                        viewModel.playMusic()
                    }else{
                        binding.playImv.setImageResource(R.drawable.ic_play_now)
                        viewModel.pauseMusic()
                        onStopAnim()
                    }
                })
                setupClickListeners()
            }
        }
    }


    private fun setupObservers() {
        viewModel.track.observe(this, Observer { track ->
            track?.let {
                binding.nameArtistTXT.text = it.artists.joinToString(", ") { artist -> artist.name }
                binding.songNameTXT.text = it.name
                Glide.with(binding.root).load(it.thumbnail).into(binding.imvTrack)
            }
        })
        viewModel.currentPosition.observe(this, Observer { currentPosition ->
            currentPosition?.let {
                binding.seekBar.progress = it
                binding.startDurationTXT.text = formatTime(it.toLong())
            }
        })

        viewModel.isPlaying.observe(this, Observer { isPlaying ->
            Log.d("isPlaying", "PlayMusic isPlaying $isPlaying")
            isPlaying?.let {
                isPlayingSend = isPlaying
                binding.playImv.setImageResource(if (it) R.drawable.ic_pause else R.drawable.ic_play)
                if (it) {
                    onStartAnim()
                } else {
                    onStopAnim()
                }
            }
        })

        viewModel.shuffle.observe(this, Observer { shuffle ->
            shuffle?.let {
                binding.shuffleBtn.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, if (it) R.color.primary else R.color.white)
                )
            }
        })

        viewModel.repeat.observe(this, Observer { repeat ->
            repeat?.let {
                binding.repeatBtn.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, if (it) R.color.primary else R.color.white)
                )
            }
        })

        viewModel.duration.observe(this, Observer { duration ->
            duration?.let {
                binding.seekBar.max = it
                binding.endDurationTXT.text = formatTime(it.toLong())
            }
        })

        viewModel.backgroundGradient.observe(this, Observer { gradientDrawable ->
            gradientDrawable?.let {
                binding.root.background = it
            }
        })
        viewModel.musicService.observe(this, Observer {
            musicServiceSend = it
        })

        viewModel.listTrackResponse.observe(this, Observer {
            listTracksSend = it
        })

        viewModel.positionTrackResponse.observe(this, Observer {
            positionTrackSend = it!!
        })


    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener { finish() }
        binding.shuffleBtn.setOnClickListener { viewModel.toggleShuffle() }
        binding.repeatBtn.setOnClickListener { viewModel.toggleRepeat() }
        binding.nextBtn.setOnClickListener { viewModel.nextTrack() }
        binding.previousBtn.setOnClickListener { viewModel.previousTrack() }
        binding.playImv.setOnClickListener {
            if (viewModel.isPlaying.value == true) {
                viewModel.pauseMusic()
                viewModel.updateIsPlaying(isPlayingSend)
                musicServiceSend?.showNotification(R.drawable.ic_play_now)
                onStopAnim()
            } else {
                viewModel.playMusic()
                isPlayingSend = true
                viewModel.updateIsPlaying(isPlayingSend)
                musicServiceSend?.showNotification(R.drawable.ic_pause_now)
                onStartAnim()
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.musicService.value?.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        val musicService = binder.currentService()
        viewModel.setMusicService(musicService)

        Executors.newSingleThreadExecutor().execute {
            runOnUiThread {
                viewModel.setPositionTrack(positionTrack)
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        viewModel.musicService.value?.mediaPlayer = null
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

    @SuppressLint("DefaultLocale")
    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private val updateIsPlayingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isPlaying = intent?.getBooleanExtra("isPlaying", false) ?: false
            viewModel.updateIsPlaying(isPlaying)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateIsPlayingReceiver)
    }

}