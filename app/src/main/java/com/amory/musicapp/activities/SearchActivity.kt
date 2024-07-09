package com.amory.musicapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
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
        setupSearch(binding.searchET) { query ->
            if (query.isEmpty()) {
                listArtist.clear()
                listTrack.clear()
                setupRecyclerViewArtistSearch()
                setupRecyclerViewTrackSearch()
            } else {
                searchArtist(query)
                searchTrack(query)
            }
        }
    }

    private fun EditText.textChanges(): Flow<CharSequence> = callbackFlow {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    trySend(s)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        }
        addTextChangedListener(watcher)
        awaitClose { removeTextChangedListener(watcher) }
    }

    @OptIn(FlowPreview::class)
    fun setupSearch(editText: EditText, searchFunction: (String) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            editText.textChanges()
                //Doi 300ms sau lan thay doi cuoi
                .debounce(300)
                .filter { it.isEmpty().not() }
                .collect {
                    searchFunction.toString()
                }
        }
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