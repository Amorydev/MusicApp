package com.amory.musicapp.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity

class MusicService : Service() {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession : MediaSessionCompat

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext,"BeatBuddy")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }
    @SuppressLint("ForegroundServiceType")
    fun showNotification(){
        var artists = ""
        for (i in 0 until PlayMusicActivity.track!!.artists.size){
            artists = PlayMusicActivity.track!!.artists[i].name
        }
        val notification = NotificationCompat.Builder(baseContext,ApplicationClass.CHANNEL_ID)
            .setContentTitle(PlayMusicActivity.track?.name)
            .setContentText(artists)
            .setSmallIcon(R.drawable.ic_home)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_logo))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_previous_music,"Previous",null)
            .addAction(R.drawable.ic_play_now,"Play",null)
            .addAction(R.drawable.ic_next_music,"Next",null)
            .addAction(R.drawable.ic_exit,"Exit",null)
            .build()
        startForeground(13,notification)
    }
}