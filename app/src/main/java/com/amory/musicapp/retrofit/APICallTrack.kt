package com.amory.musicapp.retrofit

import com.amory.musicapp.model.AddLikeResponse
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Path

interface APICallTrack {
    @POST("v1/tracks/{id}/like")
    fun addLikeMusic(
        @Path("id") id:String
    ):Call<AddLikeResponse>
}