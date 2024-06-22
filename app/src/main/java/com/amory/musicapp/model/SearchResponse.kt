package com.amory.musicapp.model

data class SearchResponse(
    val results: Search,
    val types: MutableList<String>,
    val page : Int ,
    val size : Int
)
