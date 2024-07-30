package com.amory.musicapp.managers

import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.amory.musicapp.Interface.OnCLickTrack
import com.amory.musicapp.activities.PlayMusicActivity.Companion.binding
import com.amory.musicapp.adapter.PopularTrackAdapter
import com.amory.musicapp.model.AddLikeResponse
import com.amory.musicapp.model.Track
import com.amory.musicapp.model.TrackResponse
import com.amory.musicapp.retrofit.APICallArtists
import com.amory.musicapp.retrofit.APICallCatalog
import com.amory.musicapp.retrofit.APICallTrack
import com.amory.musicapp.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object TrackManager {
    fun getTrack(page: Int, size: Int, callback: (List<Track>?) -> Unit) {
        val service = RetrofitClient.retrofitInstance.create(APICallCatalog::class.java)
        val callPopularTrack = service.getPopularTrack(page, size)
        callPopularTrack.enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (response.isSuccessful) {
                    val itemTrack = response.body()?.items
                    Log.d("trackPopular", response.body()?.items.toString())
                    callback(itemTrack)
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
            }
        })
    }

    fun getTrackOfArtist(artistId: String, page: Int, size: Int, callback: (List<Track>) -> Unit) {
        val service = RetrofitClient.retrofitInstance.create(APICallArtists::class.java)
        val call = service.getTrackOfArtist(artistId, 1, 10)
        call.enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (response.isSuccessful) {
                    val listTracks: List<Track> = response.body()?.items!! ?: emptyList()
                    callback(listTracks)
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
            }
        })
    }

    fun addLikeMusic(id: String, callback: (Boolean?) -> Unit) {
        val service = RetrofitClient.retrofitInstance.create(APICallTrack::class.java)
        val callAddLikeMusic = service.addLikeMusic(id)
        callAddLikeMusic.enqueue(object : Callback<AddLikeResponse> {
            override fun onFailure(call: Call<AddLikeResponse>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<AddLikeResponse>,
                response: Response<AddLikeResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.status == "OK") {
                        callback(true)
                    }
                }
            }
        })
    }
}