package com.amory.musicapp.retrofit

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context:Context): Interceptor {
    private var tokenClient : String ?=null
    private var tokenAuth : String ?=null
    private var token : String ?=null
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("SAVE_TOKEN",Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {

        tokenClient = sharedPreferences.getString("tokenClient","")
        tokenAuth = sharedPreferences.getString("tokenAuth","")

        token = if (tokenAuth != null){
            tokenAuth
        }else{
            tokenClient
        }
        val request = chain.request()
        val builder = request.newBuilder()
        builder.addHeader("Authorization", "Bearer $token")
        val response = chain.proceed(builder.build())
        if (response.isSuccessful){
            return response
        }
        return response
    }

}