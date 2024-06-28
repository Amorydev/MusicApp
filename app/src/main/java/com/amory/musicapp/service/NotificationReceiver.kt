package com.amory.musicapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import kotlin.system.exitProcess

class NotificationReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PLAY -> {
                if (PlayMusicActivity.isPlaying){
                    pauseMusic()
                }else{
                    playMusic()
                }
            }
            ApplicationClass.NEXT -> {
                Toast.makeText(context,"Next clicker",Toast.LENGTH_SHORT).show()
            }
            ApplicationClass.PREVIOUS -> {
                Toast.makeText(context,"Previoud clicker",Toast.LENGTH_SHORT).show()
            }
            ApplicationClass.EXIT -> {
                PlayMusicActivity.musicService?.stopForeground(true)
                PlayMusicActivity.musicService = null
                exitProcess(1)
            }
        }
    }

    private fun playMusic(){
        PlayMusicActivity.isPlaying = true
        PlayMusicActivity.musicService!!.mediaPlayer!!.start()
        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause_now)
        PlayMusicActivity.binding.playImv.setImageResource(R.drawable.ic_pause)
    }
    private fun pauseMusic(){
        PlayMusicActivity.isPlaying = false
        PlayMusicActivity.musicService!!.mediaPlayer!!.stop()
        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_play_now)
        PlayMusicActivity.binding.playImv.setImageResource(R.drawable.ic_play)
    }
}