package com.amory.musicapp.model

data class DetailPlaylistResponse(
    val id: String,
    val urn: String,
    val name: String,
    val thumbnail: String,
    val description: String,
    val isPublic: Boolean,
    val owner: OwnerPlaylist,
    val totalLikes: Int,
    val items: List<Track>,
    val scopePermissions: ScopePermissionRequest
)
