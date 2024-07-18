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

import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer

import com.amory.musicapp.R
import com.amory.musicapp.databinding.ActivityPlayMusicBinding
import com.amory.musicapp.model.Track

import com.amory.musicapp.service.MusicService
import com.amory.musicapp.viewModel.PlayMusicViewModel
import com.bumptech.glide.Glide

import java.util.concurrent.Executors

class PlayMusicActivity : AppCompatActivity(), ServiceConnection {
    companion object {
        var listTrack: List<Track>? = null
        var positionTrack: Int = 0
    }

    private val viewModel: PlayMusicViewModel by viewModels()
    private lateinit var binding: ActivityPlayMusicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        positionTrack = intent.getIntExtra("positionTrack", 0)
        viewModel.setPositionTrack(positionTrack)

        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.track.observe(this, Observer { track ->
            track?.let {
                binding.nameArtistTXT.text = it.artists.joinToString(", ") { artist -> artist.name }
                binding.songNameTXT.text = it.name
                Glide.with(binding.root).load(it.thumbnail).into(binding.imvTrack)
                Log.d("hihi",track.toString())
            }
        })

        viewModel.isPlaying.observe(this, Observer { isPlaying ->
            Log.d("isPlaying",isPlaying.toString())
            isPlaying?.let {
                binding.playImv.setImageResource(if (it) R.drawable.ic_pause_now else R.drawable.ic_play_now)
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

        viewModel.currentPosition.observe(this, Observer { currentPosition ->
            currentPosition?.let {
                binding.seekBar.progress = it
                binding.startDurationTXT.text = formatTime(it.toLong())
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
            } else {
                viewModel.playMusic()
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
/*
            viewModel.musicService?.showNotification(R.drawable.ic_pause_now)
*/
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

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
    }

}