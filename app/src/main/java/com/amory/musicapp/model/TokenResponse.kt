package com.amory.musicapp.model

data class TokenResponse(
    val token:String,
    val tokenType:String,
    val expiresIn:Int
)
