package com.amory.musicapp.retrofit

import com.amory.musicapp.model.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APICallSearch {
    @GET("v1/catalog/search")
    fun search(
        @Query("query") search: String,
        @Query("types") type: MutableList<String>,
        @Query("page") page: Int,
        @Query("size") size: Int
    ):Call<SearchResponse>
}