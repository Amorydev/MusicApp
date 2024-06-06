package com.amory.musicapp.retrofit

import com.amory.musicapp.model.TokenResponse
import retrofit2.Call
import retrofit2.http.GET

interface APICallToken {
    @GET("v1/auth/token/client-token")
    fun getToken(): Call<TokenResponse>
}