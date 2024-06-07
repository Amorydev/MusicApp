package com.amory.musicapp.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.amory.musicapp.R
import com.amory.musicapp.adapter.PopularTrackAdapter
import com.amory.musicapp.databinding.FragmentDetailArtistBinding
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.Track
import com.amory.musicapp.model.TrackResponse
import com.amory.musicapp.retrofit.APICallArtists
import com.amory.musicapp.retrofit.RetrofitClient
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailArtistFragment : Fragment() {
    private var _binding: FragmentDetailArtistBinding? = null
    private val binding get() = _binding!!
    private lateinit var artist:Artists
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailArtistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        showNameHint()
        onBack()
    }

    private fun onBack() {
        binding.backIMV.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun showNameHint() {
        binding.txtNameHint.text = artist.name
        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            if (totalScrollRange + verticalOffset == 0) {
                binding.txtNameHint.visibility = View.VISIBLE
            } else {
                binding.txtNameHint.visibility = View.GONE
            }
        })
    }

    private fun initViews() {
        artist = arguments?.getSerializable("selectedArtist") as Artists
        Log.d("artist", artist.toString())
        binding.nameArtistTXT.text = artist.name
        Glide.with(binding.root).load(artist.thumbnail).into(binding.profileArtistImv)
        showRv(artist)
    }

    private fun showRv(artists: Artists) {
        val service = RetrofitClient.retrofitInstance.create(APICallArtists::class.java)
        val call = service.getTrackOfArtist(artists.id, 1, 10)
        call.enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                val listTracks: MutableList<Track> = response.body()?.items!!
                val adapter = PopularTrackAdapter(listTracks)
                binding.detailArtistRV.adapter = adapter
                binding.detailArtistRV.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.detailArtistRV.setHasFixedSize(true)
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }
}