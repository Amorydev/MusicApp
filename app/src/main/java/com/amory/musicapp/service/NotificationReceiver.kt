package com.amory.musicapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.fragment.NowPlayingFragment
import com.amory.musicapp.managers.UriAudioManger
import com.amory.musicapp.model.Track
import com.bumptech.glide.Glide
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
                /*Toast.makeText(context,"Next clicker",Toast.LENGTH_SHORT).show()*/
                nextOrPreviousMusic(false)
            }
            ApplicationClass.PREVIOUS -> {
                /*Toast.makeText(context,"Previoud clicker",Toast.LENGTH_SHORT).show()*/
                nextOrPreviousMusic(true)
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

    private fun nextOrPreviousMusic(increment: Boolean) {
        PlayMusicActivity.setSongPosition(increment)
        val listTracks = PlayMusicActivity.listTrack
        val positionTrack = PlayMusicActivity.positionTrack
        listTracks?.get(positionTrack)?.let {
            UriAudioManger.getUriAudio(it) { uriAudio ->
                uriAudio?.let { uri ->
                    PlayMusicActivity.musicService?.let { service ->
                        service.mediaPlayer?.apply {
                            reset()
                            setDataSource(uri)
                            prepareAsync()
                        }
                    }
                }
            }
        }
        PlayMusicActivity.musicService?.showNotification(R.drawable.ic_pause_now)
        setLayout(listTracks, positionTrack)
        playMusic()
    }
    private fun setLayout(listTrack : MutableList<Track>?, positionTrack : Int){
        listTrack.let { tracks ->
            tracks!![positionTrack].let {
                PlayMusicActivity.binding.nameArtistTXT.text = it.artists.joinToString(", ") { artist -> artist.name }
                PlayMusicActivity.binding.songNameTXT.text = it.name
                Glide.with(PlayMusicActivity.binding.root).load(it.thumbnail).into(PlayMusicActivity.binding.imvTrack)
                PlayMusicActivity.binding.seekBar.progress = 0
            }
        }
    }
}