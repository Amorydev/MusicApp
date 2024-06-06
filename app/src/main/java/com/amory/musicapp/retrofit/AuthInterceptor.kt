package com.amory.musicapp.retrofit

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context:Context): Interceptor {
    private var token : String ?=null
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("SAVE_TOKEN",Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        token = sharedPreferences.getString("token","")
        val request = chain.request()
        val builder = request.newBuilder()
        builder.addHeader("X-Client-Token", "Bearer $token")
        val response = chain.proceed(builder.build())
        if (response.isSuccessful){
            return response
        }
        return response
    }

}