package com.amory.musicapp.model

data class Playlist(
    val id: String,
    val urn: String,
    val name: String,
    val thumbnail: String,
    val description: String,
    val isPublic: Boolean,
    val ownerId: String,
    val totalLikes: Int,
    val itemUrns: MutableList<String>
)
