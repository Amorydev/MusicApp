package com.amory.musicapp.managers

import android.util.Log
import com.amory.musicapp.model.ArtistResponse
import com.amory.musicapp.model.Artists
import com.amory.musicapp.retrofit.APICallCatalog
import com.amory.musicapp.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ArtistManager {
    fun getArtist(page:Int , size : Int , callback: (MutableList<Artists>?) -> Unit){
        val service = RetrofitClient.retrofitInstance.create(APICallCatalog::class.java)
        val callPopularArtist = service.getPopularArtists(page, size)
        callPopularArtist.enqueue(object : Callback<ArtistResponse> {
            override fun onResponse(
                call: Call<ArtistResponse>,
                response: Response<ArtistResponse>
            ) {
                if (response.isSuccessful) {
                    val itemArtists = response.body()?.items
                    Log.d("trackArtist", response.body()?.items.toString())
                    callback(itemArtists)
                }
            }

            override fun onFailure(call: Call<ArtistResponse>, t: Throwable) {
            }
        })
    }
}