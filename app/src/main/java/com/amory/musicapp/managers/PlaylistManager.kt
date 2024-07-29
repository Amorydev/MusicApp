package com.amory.musicapp.managers

import android.util.Log
import com.amory.musicapp.model.AddItemPlaylistResponse
import com.amory.musicapp.model.AddPlaylistResponse
import com.amory.musicapp.model.DetailPlaylistResponse
import com.amory.musicapp.model.Playlist
import com.amory.musicapp.model.PlaylistResponse
import com.amory.musicapp.retrofit.APICallPlaylist
import com.amory.musicapp.retrofit.RetrofitClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object PlaylistManager {
    val service: APICallPlaylist =
        RetrofitClient.retrofitInstance.create(APICallPlaylist::class.java)

    fun addPlaylist(
        name: RequestBody,
        isPublic: RequestBody,
        thumbnail: RequestBody,
        description: RequestBody,
        callback: (Boolean?) -> Unit
    ) {
        val callAddPlaylist = service.createPlaylist(name, isPublic, thumbnail, description)
        callAddPlaylist.enqueue(object : Callback<AddPlaylistResponse> {
            override fun onResponse(
                call: Call<AddPlaylistResponse>,
                response: Response<AddPlaylistResponse>
            ) {
                val isSuccess = true
                callback(isSuccess)
            }

            override fun onFailure(call: Call<AddPlaylistResponse>, t: Throwable) {
                Log.d("AddPlaylist", t.message.toString())
            }
        })
    }

    fun getAllPlaylist(page: Int, size: Int, callback: (MutableList<Playlist>) -> Unit) {
        val callPlaylistMe = service.getAllPlaylist(page, size)
        callPlaylistMe.enqueue(object : Callback<PlaylistResponse> {
            override fun onFailure(call: Call<PlaylistResponse>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<PlaylistResponse>,
                response: Response<PlaylistResponse>
            ) {
                if (response.isSuccessful) {
                    val listPlaylist = response.body()?.items
                    callback(listPlaylist!!)
                }
            }
        })
    }

    fun getPlaylistById(id: String, callback: (DetailPlaylistResponse?) -> Unit) {
        val callPlaylistById = service.getPlaylistById(id)
        callPlaylistById.enqueue(object : Callback<DetailPlaylistResponse> {
            override fun onFailure(call: Call<DetailPlaylistResponse>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<DetailPlaylistResponse>,
                response: Response<DetailPlaylistResponse>
            ) {
                if (response.isSuccessful) {
                    val value = response.body()
                    callback(value)
                }
            }
        })
    }

    fun addItemInPlayList(id: String, requestBody: RequestBody, callback: (Boolean) -> Unit) {
        val callAddItemPlaylist = service.addItemPlaylist(id, requestBody)
        callAddItemPlaylist.enqueue(object : Callback<AddItemPlaylistResponse>{
            override fun onResponse(
                call: Call<AddItemPlaylistResponse>,
                response: Response<AddItemPlaylistResponse>
            ) {
                if (response.isSuccessful){
                    if (response.body()?.status.equals("100 CONTINUE")){
                        callback(true)
                    }
                }
            }

            override fun onFailure(call: Call<AddItemPlaylistResponse>, t: Throwable) {
            }
        })
    }

}