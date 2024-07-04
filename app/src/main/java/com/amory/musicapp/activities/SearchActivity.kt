package com.amory.musicapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.amory.musicapp.Interface.OnCLickArtist
import com.amory.musicapp.Interface.OnCLickTrack
import com.amory.musicapp.adapter.SearchArtistAdapter
import com.amory.musicapp.adapter.SearchTrackAdapter
import com.amory.musicapp.databinding.ActivitySearchBinding
import com.amory.musicapp.managers.SearchManager
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.SearchResponse
import com.amory.musicapp.model.Track
import com.amory.musicapp.model.eventBus.EventPostListTrack
import com.amory.musicapp.retrofit.APICallSearch
import com.amory.musicapp.retrofit.RetrofitClient
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var listArtist: MutableList<Artists>
    private lateinit var listTrack: MutableList<Track>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listTrack = mutableListOf()
        listArtist = mutableListOf()
        onSearch()
    }

    private fun onSearch() {
        binding.searchET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 0) {
                    listArtist.clear()
                    listTrack.clear()
                } else {
                    searchArtist(s.toString())
                    searchTrack(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun searchArtist(search: String) {
        SearchManager.getArtistSearch(search) { artist ->
            listArtist = artist!!
            setupRecyclerViewArtistSearch()
        }
    }

    private fun setupRecyclerViewArtistSearch() {
        val adapterArtists = SearchArtistAdapter(listArtist, object : OnCLickArtist {
            override fun onCLickArtist(position: Int) {

            }
        })
        binding.searchArtists.adapter = adapterArtists
        binding.searchArtists.layoutManager =
            LinearLayoutManager(this@SearchActivity, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun searchTrack(search: String) {
        SearchManager.getTrackSearch(search) { track ->
            listTrack = track!!
            setupRecyclerViewTrackSearch()
        }
    }

    private fun setupRecyclerViewTrackSearch() {
        val adapterArtists = SearchTrackAdapter(listTrack, object : OnCLickTrack {
            override fun onCLickTrack(position: Int) {
                val itemTrack: MutableList<Track> = mutableListOf()
                itemTrack.add(listTrack[position])
                EventBus.getDefault().postSticky(EventPostListTrack(itemTrack))
                val intent = Intent(this@SearchActivity, PlayMusicActivity::class.java)
                intent.putExtra("positionTrack", 0)
                startActivity(intent)
                finish()
            }
        })
        binding.searchTrack.adapter = adapterArtists
        binding.searchTrack.layoutManager =
            LinearLayoutManager(this@SearchActivity, LinearLayoutManager.VERTICAL, false)
    }
}