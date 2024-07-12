package com.amory.musicapp.model

data class PlaylistResponse(
    val items : MutableList<Playlist>,
    val page:Int,
    val size:Int,
    val numOfItems:Int,
    val totalItems:Int,
    val totalPages:Int
)
