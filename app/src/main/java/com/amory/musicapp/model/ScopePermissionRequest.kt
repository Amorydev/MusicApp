package com.amory.musicapp.model

data class ScopePermissionRequest(
    val scope:String,
    val isGranted:Boolean,
    val user:User
)
