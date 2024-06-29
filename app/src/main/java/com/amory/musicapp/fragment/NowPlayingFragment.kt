package com.amory.musicapp.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.databinding.FragmentNowPlayingBinding
import com.bumptech.glide.Glide

class NowPlayingFragment : Fragment() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var _binding: FragmentNowPlayingBinding? = null
        val binding get() = _binding!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        binding.root.visibility = View.INVISIBLE
        binding.imvPlay.setOnClickListener {
            if (PlayMusicActivity.isPlayingMusic) {
                pauseMusic()
            } else {
                playMusic()
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkPlay() {
        if (PlayMusicActivity.isPlayingMusic) {
            binding.imvPlay.setImageResource(R.drawable.ic_pause_now)
        } else {
            binding.imvPlay.setImageResource(R.drawable.ic_play_now)
        }
    }

    override fun onResume() {
        super.onResume()
        if (PlayMusicActivity.musicService != null) {
            binding.root.visibility = View.VISIBLE
        }
        val position = PlayMusicActivity.positionTrack
        if (PlayMusicActivity.listTrack != null) {
            binding.nameArtistTXT.text =
                PlayMusicActivity.listTrack!![position].artists.joinToString(", ") { it.name }
            binding.songNameTXT.text = PlayMusicActivity.listTrack!![position].name
            Glide.with(binding.root).load(PlayMusicActivity.listTrack!![position].thumbnail)
                .into(binding.imvTrack)
            checkPlay()
        }
    }

    private fun playMusic() {
        PlayMusicActivity.isPlayingMusic = true
        PlayMusicActivity.musicService?.mediaPlayer?.apply {
            start()
            binding.imvPlay.setImageResource(R.drawable.ic_pause_now)
            PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_pause_now)
        }
    }

    private fun pauseMusic() {
        PlayMusicActivity.isPlayingMusic = false
        PlayMusicActivity.musicService?.mediaPlayer?.apply {
            pause()
            binding.imvPlay.setImageResource(R.drawable.ic_play_now)
            PlayMusicActivity.musicService!!.showNotification(R.drawable.ic_play_now)
        }
    }
}
