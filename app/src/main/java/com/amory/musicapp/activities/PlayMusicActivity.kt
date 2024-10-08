package com.amory.musicapp.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.amory.musicapp.R
import com.amory.musicapp.databinding.ActivityPlayMusicBinding
import com.amory.musicapp.model.Track
import com.amory.musicapp.service.MusicService
import com.amory.musicapp.viewModel.PlayMusicViewModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog


class PlayMusicActivity : AppCompatActivity(), ServiceConnection {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayMusicBinding
        var _isPlaying: Boolean? = null
        var musicServiceSend: MusicService? = null
        var listTracksSend: List<Track>? = null
        var positionTrackSend: Int = 0
        var likeSend: Boolean? = null
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

        viewModel.isPlaying.observe(this) {
            _isPlaying = it!!
        }
        _isPlaying?.let { viewModel.updateIsPlaying(it) }

        viewModel.like.observe(this) {
            Log.d("like", "it ${it.toString()}")
            likeSend = it!!
            Log.d("like", "likeSend $likeSend")
        }
        likeSend?.let { viewModel.updateLike(it) }
    }

    private fun initView() {
        when (intent.getStringExtra("class")) {
            "HomeFragment" -> {
                setupObservers()
                setupClickListeners()
                viewModel.setTrackChangedFromHome(true)
                viewModel.setPositionTrack(positionTrack)
                viewModel.playMusicIfNotPlaying()
            }
            "DetailArtistFragment" ->{
                setupObservers()
                setupClickListeners()
                viewModel.setPositionTrack(positionTrack)
                viewModel.playMusicIfNotPlaying()
            }

            "NowPlayingFragment" -> {
                val currentPosition = intent.getIntExtra("currentPosition", 0)
                viewModel.updateCurrentPosition(currentPosition)

                val positionTrack = intent.getIntExtra("positionTrack",0)
                Log.d("position", positionTrack.toString())
                viewModel.updatePositionTrack(positionTrack)

                viewModel.musicService.observe(this, Observer { musicService ->
                    binding.startDurationTXT.text =
                        musicService?.mediaPlayer?.currentPosition?.toLong()?.let { formatTime(it) }
                    binding.endDurationTXT.text =
                        musicService?.mediaPlayer?.duration?.toLong()?.let { formatTime(it) }
                    binding.seekBar.progress = musicService?.mediaPlayer?.currentPosition!!
                    binding.seekBar.max = musicService.mediaPlayer?.duration!!
                    musicService.mediaPlayer?.seekTo(currentPosition)
                })
                if (_isPlaying == true) {
                    binding.playImv.setImageResource(R.drawable.ic_pause)
                    viewModel.playMusic()
                    viewModel.startSeekBarUpdate()
                } else {
                    binding.playImv.setImageResource(R.drawable.ic_play)
                    viewModel.pauseMusic()
                }
                setupObservers()
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
                Log.d("currentTrack", track.toString())
            }
        })

        viewModel.currentPosition.observe(this, Observer { currentPosition ->
            currentPosition?.let {
                binding.seekBar.progress = it
                binding.startDurationTXT.text = formatTime(it.toLong())
            }
        })

        viewModel.isPlaying.observe(this, Observer { isPlaying ->
            isPlaying?.let {
                binding.playImv.setImageResource(if (it) R.drawable.ic_pause else R.drawable.ic_play)
                if (it) {
                    musicServiceSend?.showNotification(R.drawable.ic_pause_now)
                } else {
                    musicServiceSend?.showNotification(R.drawable.ic_play_now)
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

        viewModel.like.observe(this) { like ->
            like?.let {
                likeSend = like
                if (it) {
                    binding.likeBTN.setImageResource(R.drawable.ic_love)
                } else {
                    binding.likeBTN.setImageResource(R.drawable.ic_no_love)
                }
            }
        }

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
            if (it?.mediaPlayer?.isPlaying == true) {
                onStartAnim()
            } else {
                onStopAnim()
            }
        })

        viewModel.listTrackResponse.observe(this, Observer {
            listTracksSend = it
        })

        viewModel.positionTrackResponse.observe(this, Observer {
            positionTrackSend = it!!
        })
    }

    @SuppressLint("ResourceAsColor")
    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener { finish() }
        binding.shuffleBtn.setOnClickListener { viewModel.toggleShuffle() }
        binding.repeatBtn.setOnClickListener { viewModel.toggleRepeat() }
        binding.nextBtn.setOnClickListener { viewModel.nextTrack() }
        binding.previousBtn.setOnClickListener { viewModel.previousTrack() }
        binding.addPlaylistBtn.setOnClickListener { showDialog() }
        binding.likeBTN.setOnClickListener {
            viewModel.toggleLike()
            viewModel.like.observe(this){like ->
                if (like!!){
                   viewModel.addLikeMusic()
                }else{
                    viewModel.unLikeMusic()
                }
            }
        }
        binding.playImv.setOnClickListener {
            if (viewModel.isPlaying.value == true) {
                viewModel.pauseMusic()
                onStopAnim()
            } else {
                viewModel.playMusic()
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

    private fun showDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.layout_bottomsheet_dialog_additem)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialog.show()
        dialog.window?.attributes?.windowAnimations = R.style.BottomSheetDialog
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        val musicService = binder.currentService()
        viewModel.setMusicService(musicService)

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
