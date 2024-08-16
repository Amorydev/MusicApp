package com.amory.musicapp.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.amory.musicapp.Interface.OnClickBtnAddMusicInPlaylist
import com.amory.musicapp.R
import com.amory.musicapp.adapter.AddItemInPlaylistAdapter
import com.amory.musicapp.databinding.FragmentAddItemInPlaylistBinding
import com.amory.musicapp.model.Track
import com.amory.musicapp.viewModel.AddItemInPlaylistViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class AddItemInPlaylistFragment : Fragment() {
    private var _binding: FragmentAddItemInPlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddItemInPlaylistViewModel by viewModels<AddItemInPlaylistViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddItemInPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onSearch()
        viewModel.getPopularTrack()
        observer()
    }

    private fun observer() {
        viewModel.listTracksTemp.observe(viewLifecycleOwner) { lisTrackTemp ->
            setUpRecyclerViewSearchResult(lisTrackTemp)
        }
        viewModel.listTracks.observe(viewLifecycleOwner) { listTracks ->
            setUpRecyclerViewSearchResult(listTracks)
        }
    }

    private fun setUpRecyclerViewSearchResult(listTracks: List<Track?>?) {
        val adapter = AddItemInPlaylistAdapter(listTracks!!, object : OnClickBtnAddMusicInPlaylist {
            override fun onClickBtnAddMusicInPlaylist(position: Int) {
                /*Toast.makeText(requireContext(), "add music click", Toast.LENGTH_SHORT).show()*/
                val id = arguments?.getString("id")
                val idTrack = listTracks[position]?.urn?.let {
                    RequestBody.create(
                        "text/plain".toMediaTypeOrNull(),
                        it
                    )
                }
                id?.let {
                    if (idTrack != null) {
                        viewModel.addItemInPlayList(it, idTrack)
                    }
                }
            }
        })
        binding.tracksRv.adapter = adapter
        binding.tracksRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.tracksRv.setHasFixedSize(true)
    }

    private fun onSearch() {
        setupSearch(binding.searchItemTXT) { query ->
            if (query.isEmpty()) {
                viewModel.clearResults()
            } else {
                viewModel.searchTracks(query)
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

}