package com.amory.musicapp.fragment

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
    private var _binding: FragmentNowPlayingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        binding.root.visibility = View.INVISIBLE
        binding.imvPlay.setOnClickListener {
            if (PlayMusicActivity.isPlayingMusic){
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
        if (PlayMusicActivity.isPlayingMusic){
            binding.imvPlay.setImageResource(R.drawable.ic_pause_now)
        } else {
            binding.imvPlay.setImageResource(R.drawable.ic_play_now)
        }
    }

    override fun onResume() {
        super.onResume()
        if (PlayMusicActivity.musicService != null){
            binding.root.visibility = View.VISIBLE
        }
        if (PlayMusicActivity.track != null) {
            binding.nameArtistTXT.text = PlayMusicActivity.track!!.artists.joinToString(", ") { it.name }
            binding.songNameTXT.text = PlayMusicActivity.track!!.name
            Glide.with(binding.root).load(PlayMusicActivity.track!!.thumbnail).into(binding.imvTrack)
        }
       checkPlay()
    }

    private fun playMusic(){
        PlayMusicActivity.musicService?.mediaPlayer?.let { mediaPlayer ->
            if (!mediaPlayer.isPlaying) {
                try {
                    mediaPlayer.start()
                    binding.imvPlay.setImageResource(R.drawable.ic_pause_now)
                } catch (e: IllegalStateException) {
                    Log.d("Error playMusic", e.message.toString())
                }
            }
        }
    }

    private fun pauseMusic(){
        PlayMusicActivity.musicService?.mediaPlayer?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                try {
                    mediaPlayer.pause()
                    binding.imvPlay.setImageResource(R.drawable.ic_play_now)
                } catch (e: IllegalStateException) {
                    Log.d("Error playMusic", e.message.toString())
                }
            }
        }
    }
}
