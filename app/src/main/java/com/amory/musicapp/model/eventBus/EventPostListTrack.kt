package com.amory.musicapp.model.eventBus

import com.amory.musicapp.model.Track

data class EventPostListTrack(
    val listTrack: List<Track>
)