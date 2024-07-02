package com.amory.musicapp.managers

import com.amory.musicapp.activities.PlayMusicActivity

object PositionSongManger {
    fun setSongPosition(increment: Boolean) {
        if (!PlayMusicActivity.repeat){
            if (increment) {
                if (PlayMusicActivity.listTrack!!.size - 1 == PlayMusicActivity.positionTrack) {
                    PlayMusicActivity.positionTrack = 0
                } else {
                    ++PlayMusicActivity.positionTrack
                }
            } else {
                if (0 == PlayMusicActivity.positionTrack) {
                    PlayMusicActivity.positionTrack = PlayMusicActivity.listTrack!!.size - 1
                } else {
                    --PlayMusicActivity.positionTrack
                }
            }
        }
    }
}