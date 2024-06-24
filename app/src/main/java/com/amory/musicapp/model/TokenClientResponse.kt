package com.amory.musicapp.model

data class TokenClientResponse(
    val token:String,
    val tokenType:String,
    val expiresIn:Int
)
