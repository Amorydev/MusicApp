package com.amory.musicapp.managers

import com.amory.musicapp.activities.PlayMusicActivity

object PositionSongManger {
    fun setSongPosition(increment: Boolean) {
            if (!increment) {
                if (PlayMusicActivity.listTracksSend!!.size - 1 == PlayMusicActivity.positionTrackSend) {
                    PlayMusicActivity.positionTrackSend = 0
                } else {
                    ++PlayMusicActivity.positionTrackSend
                }
            } else {
                if (0 == PlayMusicActivity.positionTrackSend) {
                    PlayMusicActivity.positionTrackSend = PlayMusicActivity.listTracksSend!!.size - 1
                } else {
                    --PlayMusicActivity.positionTrackSend
                }
            }
    }
}