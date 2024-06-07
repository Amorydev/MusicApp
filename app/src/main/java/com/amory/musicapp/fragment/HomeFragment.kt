package com.amory.musicapp.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.amory.musicapp.Interface.OnCLickArtist
import com.amory.musicapp.R
import com.amory.musicapp.adapter.PopularArtistsAdapter
import com.amory.musicapp.adapter.PopularTrackAdapter
import com.amory.musicapp.databinding.FragmentHomeBinding
import com.amory.musicapp.model.ArtistResponse
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.Track
import com.amory.musicapp.model.TrackResponse
import com.amory.musicapp.retrofit.APICallCatalog
import com.amory.musicapp.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding ?= null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.searchET.requestFocus()
        getPopularTracks()
        getPopularArtist()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    private fun getPopularTracks(){
        val service = RetrofitClient.retrofitInstance.create(APICallCatalog::class.java)
        val callPopularTrack = service.getPopularTrack(1,5)
        callPopularTrack.enqueue(object : Callback<TrackResponse>{
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (response.isSuccessful){
                    val items: MutableList<Track>? = response.body()?.items
                    Log.d("trackPopular",response.body()?.items.toString())
                    val adapter = PopularTrackAdapter(items!!)
                    binding.popularTracks.adapter = adapter
                    binding.popularTracks.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
                    binding.popularTracks.setHasFixedSize(true)
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
            }
        })
    }
    private fun getPopularArtist(){
        val service = RetrofitClient.retrofitInstance.create(APICallCatalog::class.java)
        val callPopularArtist = service.getPopularArtists(1,5)
        callPopularArtist.enqueue(object : Callback<ArtistResponse>{
            override fun onResponse(
                call: Call<ArtistResponse>,
                response: Response<ArtistResponse>
            ) {
                if (response.isSuccessful){
                    val items: MutableList<Artists>? = response.body()?.items
                    Log.d("trackArtist",response.body()?.items.toString())
                    val adapter = PopularArtistsAdapter(items!!, object : OnCLickArtist{
                        override fun onCLickArtist(position: Int) {
                            val selectedArtist = items[position]
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
                    binding.rvPopularArtists.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
                    binding.rvPopularArtists.setHasFixedSize(true)
                }
            }

            override fun onFailure(call: Call<ArtistResponse>, t: Throwable) {
            }
        })
    }

}