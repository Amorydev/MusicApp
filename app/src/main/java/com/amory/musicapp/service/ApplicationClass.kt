package com.amory.musicapp.service

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import java.nio.file.attribute.AclEntry.Builder

class ApplicationClass: Application() {
    companion object{
        const val CHANNEL_ID = "channel"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PAUSE = "pause"
        const val EXIT = "exit"
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(CHANNEL_ID,"Now playing music",NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "This is important channel for song"
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}