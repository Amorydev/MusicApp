package com.amory.musicapp.model

data class TrackResponse(
    val items: List<Track>,
    val page:Int,
    val size : Int,
    val numOfItems :Int,
    val totalItems:Long,
    val totalPages:Int
)
