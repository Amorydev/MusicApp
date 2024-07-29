package com.amory.musicapp.retrofit

import com.amory.musicapp.model.AddItemPlaylistResponse
import com.amory.musicapp.model.AddPlaylistResponse
import com.amory.musicapp.model.DetailPlaylistResponse
import com.amory.musicapp.model.PlaylistResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface APICallPlaylist {
    @Multipart
    @POST("v1/playlists")
    fun createPlaylist(
        @Part("name") name: RequestBody,
        @Part("isPublic") isPublic: RequestBody,
        @Part("thumbnail") thumbnail: RequestBody,
        @Part("description") description: RequestBody
    ): Call<AddPlaylistResponse>

    @GET("v1/me/playlists")
    fun getAllPlaylist(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<PlaylistResponse>

    @GET("v1/playlists/{id}")
    fun getPlaylistById(
        @Path("id") id: String
    ): Call<DetailPlaylistResponse>

    @PUT("playlists/{id}/add-item")
    fun addItemPlaylist(
        @Path("id") id:String,
        @Part("RequestBody") requestBody: RequestBody
    ): Call<AddItemPlaylistResponse>
}