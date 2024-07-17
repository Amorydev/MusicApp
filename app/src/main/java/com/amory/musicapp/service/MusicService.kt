package com.amory.musicapp.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.view.animation.LinearInterpolator
import androidx.core.app.NotificationCompat
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.util.concurrent.TimeUnit

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
   /* @SuppressLint("ForegroundServiceType")
    fun showNotification(playBtn:Int){

        val preIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prePendingIntent = PendingIntent.getBroadcast(baseContext,0,preIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext,0,exitIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val track = PlayMusicActivity.listTrack!![PlayMusicActivity.positionTrack]
        var artists = ""
        for (i in 0 until track.artists.size){
            artists = track.artists[i].name
        }
        Glide.with(baseContext)
            .asBitmap()
            .load(track.thumbnail)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val notification = NotificationCompat.Builder(baseContext,ApplicationClass.CHANNEL_ID)
                        .setContentTitle(track.name)
                        .setContentText(artists)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setLargeIcon(resource)
                        .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setOnlyAlertOnce(true)
                        .addAction(R.drawable.ic_previous_music,"Previous",prePendingIntent)
                        .addAction(playBtn,"Play",playPendingIntent)
                        .addAction(R.drawable.ic_next_music,"Next",nextPendingIntent)
                        .addAction(R.drawable.ic_exit,"Exit",exitPendingIntent)
                        .build()
                    startForeground(13,notification)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

    }*/

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}