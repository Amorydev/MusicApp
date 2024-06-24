package com.amory.musicapp.model

data class AuthResponse(
    val id:String,
    val urn:String,
    val firstName:String,
    val lastName:String,
    val picture:String,
    val username:String,
    val email:String,
    val isEmailVerified:Boolean,
    val linkedIdentityProviders:MutableList<IdentityProvider>
)
