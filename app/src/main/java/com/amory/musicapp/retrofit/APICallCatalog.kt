package com.amory.musicapp.retrofit

import com.amory.musicapp.model.ArtistResponse
import com.amory.musicapp.model.TrackResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APICallCatalog {
    @GET("v1/catalog/feed/popular-tracks")
    fun getPopularTrack(
        @Query ("page") page:Int,
        @Query ("size") size :Int
    ): Call<TrackResponse>
    @GET("v1/catalog/feed/popular-artists")
    fun getPopularArtists(
        @Query ("page") page:Int,
        @Query ("size") size :Int
    ): Call<ArtistResponse>
}