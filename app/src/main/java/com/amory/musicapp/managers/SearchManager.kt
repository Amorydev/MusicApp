package com.amory.musicapp.managers

import android.util.Log
import com.amory.musicapp.activities.PlayMusicActivity.Companion.listTrack
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.SearchResponse
import com.amory.musicapp.model.Track
import com.amory.musicapp.retrofit.APICallSearch
import com.amory.musicapp.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object SearchManager {
    fun getArtistSearch(search: String, callback: (MutableList<Artists>?) -> Unit) {
        val types: MutableList<String> = mutableListOf("artist")
        val page = 1
        val size = 3
        val service = RetrofitClient.retrofitInstance.create(APICallSearch::class.java)
        val callSearch = service.search(search, types, page, size)
        callSearch.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (response.isSuccessful) {
                    val results = response.body()?.results
                    val listArtist = results!!.artist.items
                    callback(listArtist)
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.d("Search", t.message.toString())
            }
        })

    }

    fun getTrackSearch(search: String, callback: (MutableList<Track>?) -> Unit) {
        val types: MutableList<String> = mutableListOf("track")
        val page = 1
        val size = 10
        val service = RetrofitClient.retrofitInstance.create(APICallSearch::class.java)
        val callSearch = service.search(search, types, page, size)
        callSearch.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (response.isSuccessful) {
                    val results = response.body()?.results
                    listTrack = results!!.track.items
                    Log.d("list", listTrack.toString())
                    callback(listTrack)
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.d("Search", t.message.toString())
            }
        })
    }
}