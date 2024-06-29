package com.amory.musicapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.fragment.NowPlayingFragment
import kotlin.system.exitProcess

class NotificationReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PLAY -> {
                if (PlayMusicActivity.isPlayingMusic){
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
        PlayMusicActivity.isPlayingMusic = true
        PlayMusicActivity.musicService!!.mediaPlayer!!.start()
        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause_now)
        PlayMusicActivity.binding.playImv.setImageResource(R.drawable.ic_pause)
        NowPlayingFragment.binding.imvPlay.setImageResource(R.drawable.ic_pause_now)
    }
    private fun pauseMusic(){
        PlayMusicActivity.isPlayingMusic = false
        PlayMusicActivity.musicService!!.mediaPlayer!!.pause()
        PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_play_now)
        PlayMusicActivity.binding.playImv.setImageResource(R.drawable.ic_play)
        NowPlayingFragment.binding.imvPlay.setImageResource(R.drawable.ic_play_now)

    }
}