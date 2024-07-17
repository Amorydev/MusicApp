package com.amory.musicapp.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.databinding.FragmentNowPlayingBinding
import com.amory.musicapp.viewModel.NowPlayingViewModel
import com.bumptech.glide.Glide

class NowPlayingFragment : Fragment() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var _binding: FragmentNowPlayingBinding? = null
        val binding get() = _binding!!

        private val nowPlayingViewModel = NowPlayingViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.nowPlayingViewModel = nowPlayingViewModel
       /* nowPlayingViewModel.initialize()*/
        nowPlayingViewModel.currentTracks.observe(viewLifecycleOwner, Observer { track ->
            binding.songNameTXT.text = track?.name
            binding.nameArtistTXT.text = track?.artists?.joinToString(", ") { it.name }
            Glide.with(binding.root).load(track?.thumbnail).into(binding.imvTrack)
        })

        nowPlayingViewModel.isPlaying.observe(viewLifecycleOwner, Observer { isPlaying ->
            if (isPlaying) {
                binding.imvPlay.setImageResource(R.drawable.ic_pause_now)
            } else {
                binding.imvPlay.setImageResource(R.drawable.ic_play_now)
            }
        })
    /*    binding.imvPlay.setOnClickListener {
            nowPlayingViewModel.playOrPauseMusic()
        }
        binding.nextBtn.setOnClickListener {
            nowPlayingViewModel.nextSong()
        }*/
        binding.root.visibility = View.INVISIBLE
        // Handle click on the Now Playing fragment to open PlayMusicActivity
        binding.root.setOnClickListener {
            openPlayMusicActivity()
        }
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onResume() {
        super.onResume()
       /* nowPlayingViewModel.initialize()*/
        /*if (PlayMusicActivity.musicService != null) {
            binding.root.visibility = View.VISIBLE
        }*/
        nowPlayingViewModel.currentTracks.observe(viewLifecycleOwner, Observer { track ->
            binding.songNameTXT.text = track?.name
            binding.nameArtistTXT.text = track?.artists?.joinToString(", ") { it.name }
            Glide.with(binding.root).load(track?.thumbnail).into(binding.imvTrack)
        })
        nowPlayingViewModel.isPlaying.observe(viewLifecycleOwner, Observer { isPlaying ->
            if (isPlaying) {
                binding.imvPlay.setImageResource(R.drawable.ic_pause_now)
            } else {
                binding.imvPlay.setImageResource(R.drawable.ic_play_now)
            }
        })
    }

    private fun openPlayMusicActivity() {
        val intent = Intent(requireContext(), PlayMusicActivity::class.java).apply {
/*
            putExtra("positionTrack", PlayMusicActivity.positionTrack)
*/
            putExtra("class", "NowPlaying")
           /* putExtra(
                "currentPosition",
                PlayMusicActivity.musicService?.mediaPlayer?.currentPosition
            )*/
        }
        ContextCompat.startActivity(requireContext(), intent, null)
    }
}
