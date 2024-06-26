package com.amory.musicapp.model

import android.os.Parcelable
import java.io.Serializable

data class Artists(
    val id:String,
    val urn:String,
    val name:String,
    val isVerified:Boolean,
    val isPublic:Boolean,
    val birthDate:String,
    val description:String,
    val nationality:String,
    val biography:String,
    val thumbnail:String,
    val background:String,
    val totalLikes:Int
) : Serializable
