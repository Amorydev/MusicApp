package com.amory.musicapp.retrofit

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.beatbuddy.io.vn/"
    private lateinit var authInterceptor: AuthInterceptor
    private lateinit var cache: Cache
    private const val cacheSize = (5 * 1024 * 1024).toLong() // 5 MB

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }


    private val okBuilder: OkHttpClient.Builder by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
    }
    private val gson: Gson by lazy {
        GsonBuilder().setLenient().create()
    }
    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okBuilder.build())
            .build()
    }

    fun init(context: Context) {
        authInterceptor = AuthInterceptor(context)
        cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)
    }
}