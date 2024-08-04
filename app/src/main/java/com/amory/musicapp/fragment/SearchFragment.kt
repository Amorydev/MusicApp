package com.amory.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.amory.musicapp.Interface.OnCLickArtist
import com.amory.musicapp.Interface.OnCLickTrack
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.adapter.SearchArtistAdapter
import com.amory.musicapp.adapter.SearchTrackAdapter
import com.amory.musicapp.databinding.FragmentSearchBinding
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.Track
import com.amory.musicapp.model.eventBus.EventPostListTrack
import com.amory.musicapp.viewModel.SearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel : SearchViewModel by viewModels<SearchViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onSearch()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.artists.observe(viewLifecycleOwner, Observer { artists ->
            if (artists != null) {
                setupRecyclerViewArtistSearch(artists)
            }
        })

        viewModel.tracks.observe(viewLifecycleOwner, Observer { tracks ->
            if (tracks != null) {
                setupRecyclerViewTrackSearch(tracks)
            }
        })
    }


    private fun onSearch() {
        setupSearch(binding.searchET) { query ->
            if (query.isEmpty()) {
                viewModel.clearResults()
            } else {
                viewModel.searchArtist(query)
                viewModel.searchTrack(query)
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
                    searchFunction(it.toString())
                }
        }
    }

    private fun setupRecyclerViewArtistSearch(listArtist : List<Artists>) {
        val adapterArtists = SearchArtistAdapter(listArtist, object : OnCLickArtist {
            override fun onCLickArtist(position: Int) {

            }
        })
        binding.searchArtists.adapter = adapterArtists
        binding.searchArtists.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }



    private fun setupRecyclerViewTrackSearch(tracks: List<Track>) {
        val adapterArtists = SearchTrackAdapter(tracks, object : OnCLickTrack {
            override fun onCLickTrack(position: Int) {
                val itemTrack: ArrayList<Track> = arrayListOf()
                itemTrack.add(tracks[position])
                EventBus.getDefault().postSticky(EventPostListTrack(itemTrack))
                val intent = Intent(requireContext(), PlayMusicActivity::class.java)
                intent.putExtra("positionTrack", 0)
                startActivity(intent)
                requireActivity().finish()
            }
        })
        binding.searchTrack.adapter = adapterArtists
        binding.searchTrack.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

}