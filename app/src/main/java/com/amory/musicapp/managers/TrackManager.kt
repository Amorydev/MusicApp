package com.amory.musicapp.managers

import android.util.Log
import com.amory.musicapp.model.Track
import com.amory.musicapp.model.TrackResponse
import com.amory.musicapp.retrofit.APICallCatalog
import com.amory.musicapp.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object TrackManager {
    fun getTrack(page: Int, size: Int, callback: (MutableList<Track>?) -> Unit) {
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
}