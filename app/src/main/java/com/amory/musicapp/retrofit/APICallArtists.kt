package com.amory.musicapp.retrofit

import com.amory.musicapp.model.TrackResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APICallArtists {
    @GET("v1/artists/{ids}/popular-tracks")
    fun getTrackOfArtist(
        @Path ("ids") ids:String,
        @Query("page") page:Int,
        @Query("size") size :Int
    ): Call<TrackResponse>
}