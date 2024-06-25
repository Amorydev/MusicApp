package com.amory.musicapp.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.amory.musicapp.databinding.FragmentNowPlayingBinding
import com.amory.musicapp.viewModel.SharedViewModel
import com.bumptech.glide.Glide

class NowPlayingFragment : Fragment() {
    private var _binding: FragmentNowPlayingBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        binding.root.visibility = View.INVISIBLE
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.currentTrack.observe(viewLifecycleOwner) { track ->
            Log.d("track", track.toString())
            if (track != null) {

                binding.nameArtistTXT.text = track.artists.joinToString(", ") { it.name }
                binding.songNameTXT.text = track.name
                Glide.with(binding.root).load(track.thumbnail).into(binding.imvTrack)
            }
        }

        sharedViewModel.mediaPlayer.observe(viewLifecycleOwner) { mediaPlayer ->
            if (mediaPlayer != null) {
                binding.root.visibility = View.VISIBLE
            }
        }
    }
}
