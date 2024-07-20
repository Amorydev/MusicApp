package com.amory.musicapp.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.databinding.FragmentNowPlayingBinding
import com.amory.musicapp.viewModel.NowPlayingViewModel
import com.amory.musicapp.viewModel.PlayMusicViewModel
import com.bumptech.glide.Glide

class NowPlayingFragment : Fragment() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var _binding: FragmentNowPlayingBinding? = null
        val binding get() = _binding!!

        private val nowPlayingViewModel = NowPlayingViewModel()
        private lateinit var viewModelPlayMusic: PlayMusicViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.nowPlayingViewModel = nowPlayingViewModel

        viewModelPlayMusic = ViewModelProvider(this)[PlayMusicViewModel::class.java]

        nowPlayingViewModel.init()

        initialViews()
        binding.imvPlay.setOnClickListener {
            nowPlayingViewModel.playOrPauseMusic()
            nowPlayingViewModel.isPlaying.observe(viewLifecycleOwner, Observer {
                viewModelPlayMusic.updateIsPlaying(it)
            })
        }
        binding.nextBtn.setOnClickListener {
            nowPlayingViewModel.nextSong()
        }
        binding.root.visibility = View.INVISIBLE
        // Handle click on the Now Playing fragment to open PlayMusicActivity
        binding.root.setOnClickListener {
            openPlayMusicActivity()
        }
        return binding.root
    }

    private fun initialViews() {
        nowPlayingViewModel.currentTracks.observe(viewLifecycleOwner, Observer { track ->
            binding.songNameTXT.text = track?.name
            binding.nameArtistTXT.text = track?.artists?.joinToString(", ") { it.name }
            Glide.with(binding.root).load(track?.thumbnail).into(binding.imvTrack)
        })

        viewModelPlayMusic.isPlaying.observe(viewLifecycleOwner, Observer { isPlaying ->
            Log.d("isPlaying", " viewModelPlayMusic $isPlaying")
            nowPlayingViewModel.updateIsPlaying(isPlaying!!)
            if (isPlaying == true) {
                binding.imvPlay.setImageResource(R.drawable.ic_pause_now)
            } else {
                binding.imvPlay.setImageResource(R.drawable.ic_play_now)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onResume() {
        super.onResume()
        if (PlayMusicActivity.musicServiceSend != null) {
            nowPlayingViewModel.init()
            binding.root.visibility = View.VISIBLE
            initialViews()
        }
    }

    private fun openPlayMusicActivity() {
        val intent = Intent(requireContext(), PlayMusicActivity::class.java).apply {
            putExtra("positionTrack", PlayMusicActivity.positionTrackSend)
            putExtra("class", "NowPlayingFragment")
            putExtra(
                "currentPosition",
                PlayMusicActivity.musicServiceSend?.mediaPlayer?.currentPosition
            )
        }
        startActivity(intent)
    }

}
