package com.amory.musicapp.retrofit

import com.amory.musicapp.model.AudioResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface APICallAudio {
    @GET("v1/audio/files/{id}")
    fun getAudioById(
        @Path ("id") id:String
    ):Call<AudioResponse>
}