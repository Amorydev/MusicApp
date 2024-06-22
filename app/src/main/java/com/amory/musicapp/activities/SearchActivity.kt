package com.amory.musicapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.amory.musicapp.Interface.OnCLickArtist
import com.amory.musicapp.Interface.OnCLickTrack
import com.amory.musicapp.R
import com.amory.musicapp.adapter.PopularTrackAdapter
import com.amory.musicapp.adapter.SearchArtistAdapter
import com.amory.musicapp.adapter.SearchTrackAdapter
import com.amory.musicapp.databinding.ActivitySearchBinding
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.SearchArtistRequest
import com.amory.musicapp.model.SearchResponse
import com.amory.musicapp.model.SearchTrackRequest
import com.amory.musicapp.model.Track
import com.amory.musicapp.retrofit.APICallSearch
import com.amory.musicapp.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchBinding
    private lateinit var listArtist:MutableList<Artists>
    private lateinit var listTrack:MutableList<Track>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listTrack = mutableListOf()
        listArtist = mutableListOf()
        onSearch()
    }

    private fun onSearch() {
        binding.searchET.addTextChangedListener(object  : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 0){
                    listArtist.clear()
                    listTrack.clear()
                }else{
                    searchArtist(s.toString())
                    searchTrack(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun searchArtist(search: String) {
        val types : MutableList<String> = mutableListOf("artist")
        val page = 1
        val size = 3
        val service = RetrofitClient.retrofitInstance.create(APICallSearch::class.java)
        val callSearch = service.search(search,types,page,size)
        callSearch.enqueue(object : Callback<SearchResponse>{
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (response.isSuccessful){
                    val results = response.body()?.results
                    listArtist = results!!.artist.items
                    Log.d("list",listArtist.toString())
                    val adapterArtists = SearchArtistAdapter(listArtist,object : OnCLickArtist{
                        override fun onCLickArtist(position: Int) {

                        }
                    })
                    binding.searchArtists.adapter = adapterArtists
                    binding.searchArtists.layoutManager = LinearLayoutManager(this@SearchActivity,LinearLayoutManager.HORIZONTAL,false)
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.d("Search", t.message.toString())
            }
        })
    }

    private fun searchTrack(search:String) {
        val types : MutableList<String> = mutableListOf("track")
        val page = 1
        val size = 10
        val service = RetrofitClient.retrofitInstance.create(APICallSearch::class.java)
        val callSearch = service.search(search,types,page,size)
        callSearch.enqueue(object : Callback<SearchResponse>{
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (response.isSuccessful){
                    val results = response.body()?.results
                    listTrack = results!!.track.items
                    Log.d("list",listTrack.toString())
                    val adapterArtists = SearchTrackAdapter(listTrack, object : OnCLickTrack{
                        override fun onCLickTrack(position: Int) {

                        }
                    })
                    binding.searchTrack.adapter = adapterArtists
                    binding.searchTrack.layoutManager = LinearLayoutManager(this@SearchActivity,LinearLayoutManager.VERTICAL,false)
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.d("Search", t.message.toString())
            }
        })
    }
}