package com.amory.musicapp.managers

import android.util.Log
import com.amory.musicapp.model.AudioResponse
import com.amory.musicapp.model.Track
import com.amory.musicapp.retrofit.APICallAudio
import com.amory.musicapp.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//Singleton object
object UriAudioManger {
     fun getUriAudio(track: Track, callback: (String?) -> Unit) {
        val service = RetrofitClient.retrofitInstance.create(APICallAudio::class.java)
        val audioIds = track.audioFileIds.joinToString(separator = ",")
        audioIds.let {
            val callAudio = service.getAudioById(it)
            callAudio.enqueue(object : Callback<AudioResponse> {
                override fun onResponse(
                    call: Call<AudioResponse>,
                    response: Response<AudioResponse>
                ) {
                    if (response.isSuccessful) {
                        val uriAudio = response.body()?.uris?.get(0)
                        Log.d("audioFile", uriAudio.toString())
                        callback(uriAudio)
                    }else{
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<AudioResponse>, t: Throwable) {}
            })
        }
    }
}