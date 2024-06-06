package com.amory.musicapp.model

data class Track(
    val id:String,
    val urn:String,
    val name :String,
    val durationSec: Int,
    val description:String,
    val releasedDate:String,
    val thumbnail:String,
    val isPublic:Boolean,
    val isPlayable:Boolean,
    val totalLikes:Int,
    val audioFileIds:MutableList<String>,
    val artists:MutableList<Artists>
)
