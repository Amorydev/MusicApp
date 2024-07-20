package com.amory.musicapp.service

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.palette.graphics.Palette
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.fragment.NowPlayingFragment
import com.amory.musicapp.managers.PositionSongManger.setSongPosition
import com.amory.musicapp.managers.AudioManger
import com.amory.musicapp.model.Track
import com.amory.musicapp.viewModel.PlayMusicViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ApplicationClass.PLAY -> {
                if (PlayMusicActivity.isPlayingSend) {
                    pauseMusic(context)
                } else {
                    playMusic(context)
                }
            }

            ApplicationClass.NEXT -> {
                /*Toast.makeText(context,"Next clicker",Toast.LENGTH_SHORT).show()*/
                nextOrPreviousMusic(true, context)
            }

            ApplicationClass.PREVIOUS -> {
                /*Toast.makeText(context,"Previous clicker",Toast.LENGTH_SHORT).show()*/
                nextOrPreviousMusic(false, context)
            }

            ApplicationClass.EXIT -> {
                PlayMusicActivity.musicServiceSend?.stopForeground(true)
                PlayMusicActivity.musicServiceSend = null
                exitProcess(1)
            }
        }
    }

    private fun playMusic(context: Context?) {
        PlayMusicActivity.musicServiceSend!!.mediaPlayer!!.start()
        PlayMusicActivity.musicServiceSend!!.showNotification(R.drawable.ic_pause_now)
        PlayMusicActivity.binding.playImv.setImageResource(R.drawable.ic_pause)
        NowPlayingFragment.binding.imvPlay.setImageResource(R.drawable.ic_pause_now)
        sendUpdateIsPlayingBroadcast(context, true)
    }

    private fun pauseMusic(context: Context?) {
        PlayMusicActivity.musicServiceSend!!.mediaPlayer!!.pause()
        PlayMusicActivity.musicServiceSend!!.showNotification(R.drawable.ic_play_now)
        PlayMusicActivity.binding.playImv.setImageResource(R.drawable.ic_play)
        NowPlayingFragment.binding.imvPlay.setImageResource(R.drawable.ic_play_now)
        sendUpdateIsPlayingBroadcast(context, false)
    }

    private fun nextOrPreviousMusic(increment: Boolean, context: Context?) {
        setSongPosition(increment)
        val listTracks = PlayMusicActivity.listTracksSend
        val positionTrack = PlayMusicActivity.positionTrackSend

        Log.d("NotificationReceiver", listTracks.toString())
        Log.d("NotificationReceiver", positionTrack.toString())
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
        setLayout(listTracks, positionTrack)
        playMusic(context)
    }

    private fun sendUpdateIsPlayingBroadcast(context: Context?, isPlaying: Boolean) {
        val intent = Intent("UPDATE_IS_PLAYING")
        intent.putExtra("isPlaying", isPlaying)
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
    }

    private fun setLayout(listTrack: List<Track>?, positionTrack: Int) {
        listTrack.let { tracks ->
            tracks!![positionTrack].let {
                PlayMusicActivity.binding.nameArtistTXT.text =
                    it.artists.joinToString(", ") { artist -> artist.name }
                PlayMusicActivity.binding.songNameTXT.text = it.name
                Glide.with(PlayMusicActivity.binding.root).load(it.thumbnail)
                    .into(PlayMusicActivity.binding.imvTrack)
                Glide.with(PlayMusicActivity.binding.root)
                    .asBitmap()
                    .load(listTrack!![positionTrack].thumbnail)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            Palette.from(resource).generate { palette ->
                                val dominantColor =
                                    palette?.dominantSwatch?.rgb ?: Color.TRANSPARENT
                                val gradientDrawable = GradientDrawable().apply {
                                    shape = GradientDrawable.RECTANGLE
                                    colors = intArrayOf(dominantColor, 0xFF434343.toInt())
                                }
                                PlayMusicActivity.binding.root.background = gradientDrawable
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            }
        }
    }
}