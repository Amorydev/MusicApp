package com.amory.musicapp.model

data class ArtistResponse(
    val items: MutableList<Artists>,
    val page:Int,
    val size : Int,
    val numOfItems :Int,
    val totalItems:Long,
    val totalPages:Int
)