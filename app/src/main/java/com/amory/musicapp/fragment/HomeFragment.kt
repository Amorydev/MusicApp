package com.amory.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.amory.musicapp.Interface.OnCLickArtist
import com.amory.musicapp.Interface.OnCLickTrack
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.activities.SearchActivity
import com.amory.musicapp.adapter.PopularArtistsAdapter
import com.amory.musicapp.adapter.PopularTrackAdapter
import com.amory.musicapp.databinding.FragmentHomeBinding
import com.amory.musicapp.model.ArtistResponse
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.Track
import com.amory.musicapp.model.TrackResponse
import com.amory.musicapp.model.eventBus.EventPostListTrack
import com.amory.musicapp.retrofit.APICallCatalog
import com.amory.musicapp.retrofit.RetrofitClient
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.system.exitProcess

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var itemTrack: MutableList<Track>? = null
    private var itemArtists: MutableList<Artists>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.searchET.requestFocus()
        getPopularTracks()
        getPopularArtist()
        onCLickSearch()
    }

    private fun onCLickSearch() {
        binding.searchET.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (itemTrack != null && itemArtists != null){
            setRecyclerViewPopularTracks()
            setRecyclerViewPopularArtists()
        }else{
            getPopularArtist()
            getPopularTracks()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        if (!PlayMusicActivity.isPlayingMusic && PlayMusicActivity.musicService != null){
            PlayMusicActivity.musicService!!.stopForeground(true)
            PlayMusicActivity.musicService!!.mediaPlayer!!.release()
            PlayMusicActivity.musicService!!.mediaPlayer = null
            exitProcess(1)
        }
    }

    private fun getPopularTracks() {
        val service = RetrofitClient.retrofitInstance.create(APICallCatalog::class.java)
        val callPopularTrack = service.getPopularTrack(1, 5)
        callPopularTrack.enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (response.isSuccessful) {
                    itemTrack = response.body()?.items
                    Log.d("trackPopular", response.body()?.items.toString())
                    if (_binding != null) {
                        setRecyclerViewPopularTracks()
                    }
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
            }
        })
    }

    private fun setRecyclerViewPopularTracks() {
        val adapter = PopularTrackAdapter(itemTrack!!, object : OnCLickTrack {
            override fun onCLickTrack(position: Int) {
                EventBus.getDefault().postSticky(EventPostListTrack(itemTrack!!))
                val intent = Intent(requireContext(), PlayMusicActivity::class.java)
                intent.putExtra("positionTrack", position)
                startActivity(intent)
            }
        })

        binding.popularTracks.adapter = adapter
        binding.popularTracks.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.popularTracks.setHasFixedSize(true)
    }

    private fun getPopularArtist() {
        val service = RetrofitClient.retrofitInstance.create(APICallCatalog::class.java)
        val callPopularArtist = service.getPopularArtists(1, 5)
        callPopularArtist.enqueue(object : Callback<ArtistResponse> {
            override fun onResponse(
                call: Call<ArtistResponse>,
                response: Response<ArtistResponse>
            ) {
                if (response.isSuccessful) {
                    itemArtists = response.body()?.items
                    Log.d("trackArtist", response.body()?.items.toString())
                    if (_binding != null) {
                        setRecyclerViewPopularArtists()
                    }
                }
            }

            override fun onFailure(call: Call<ArtistResponse>, t: Throwable) {
            }
        })
    }

    private fun setRecyclerViewPopularArtists() {
        val adapter = PopularArtistsAdapter(itemArtists!!, object : OnCLickArtist {
            override fun onCLickArtist(position: Int) {
                val selectedArtist = itemArtists!![position]
                val fragment = DetailArtistFragment()

                val bundle = Bundle()
                bundle.putSerializable("selectedArtist", selectedArtist)
                fragment.arguments = bundle

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        })
        binding.rvPopularArtists.adapter = adapter
        binding.rvPopularArtists.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvPopularArtists.setHasFixedSize(true)
    }

    override fun onResume() {
        super.onResume()
        if (itemArtists != null && itemTrack != null) {
            setRecyclerViewPopularTracks()
            setRecyclerViewPopularArtists()
        }else{
            getPopularArtist()
            getPopularTracks()
        }
    }

}