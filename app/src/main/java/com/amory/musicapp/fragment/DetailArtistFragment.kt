package com.amory.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.amory.musicapp.Interface.OnCLickTrack
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.adapter.PopularTrackAdapter
import com.amory.musicapp.databinding.FragmentDetailArtistBinding
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.Track
import com.amory.musicapp.model.TrackResponse
import com.amory.musicapp.model.eventBus.EventPostListTrack
import com.amory.musicapp.retrofit.APICallArtists
import com.amory.musicapp.retrofit.RetrofitClient
import com.amory.musicapp.viewModel.DetailArtistViewModel
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailArtistFragment : Fragment() {
    private var _binding: FragmentDetailArtistBinding? = null
    private val binding get() = _binding!!
    private lateinit var artist: Artists
    private val viewModel: DetailArtistViewModel by viewModels()
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
        viewModel.setArtist(artist)
        viewModel.getTracks(artist.id)
        observers()
    }

    private fun observers() {
        viewModel.artist.observe(viewLifecycleOwner) {
            binding.nameArtistTXT.text = it?.name
            Glide.with(binding.root).load(it?.thumbnail).into(binding.profileArtistImv)
        }
        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            setRecyclerviewTracks(tracks as MutableList<Track>)
        }
    }

    private fun setRecyclerviewTracks(tracks: MutableList<Track>) {
        val adapter = PopularTrackAdapter(tracks, object : OnCLickTrack {
            override fun onCLickTrack(position: Int) {
                EventBus.getDefault().postSticky(EventPostListTrack(tracks))
                val intent = Intent(requireContext(), PlayMusicActivity::class.java)
                intent.putExtra("class", "DetailArtistFragment")
                intent.putExtra("positionTrack", position)
                startActivity(intent)
            }
        })
        binding.detailArtistRV.adapter = adapter
        binding.detailArtistRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.detailArtistRV.setHasFixedSize(true)
    }

}