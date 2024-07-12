package com.amory.musicapp.retrofit

import androidx.annotation.Size
import com.amory.musicapp.model.AddPlaylistResponse
import com.amory.musicapp.model.PlaylistResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface APICallPlaylist {
    @Multipart
    @POST("v1/playlists")
    fun createPlaylist(
        @Part("name") name: RequestBody,
        @Part("isPublic") isPublic: RequestBody,
        @Part ("thumbnail") thumbnail: RequestBody,
        @Part("description") description: RequestBody
    ): Call<AddPlaylistResponse>

    @GET("v1/me/playlists")
    fun getPlaylist(
        @Query("page") page:Int,
        @Query("size") size: Int
    ): Call<PlaylistResponse>
}