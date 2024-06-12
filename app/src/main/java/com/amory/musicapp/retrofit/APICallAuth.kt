package com.amory.musicapp.retrofit

import com.amory.musicapp.model.AuthResponse
import retrofit2.Call
import retrofit2.http.GET

interface APICallAuth {
    @GET("v1/auth/me")
    fun getAccount(): Call<AuthResponse>
}