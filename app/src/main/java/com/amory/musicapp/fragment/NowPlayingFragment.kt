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
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.databinding.FragmentNowPlayingBinding
import com.amory.musicapp.managers.PositionSongManger.setSongPosition
import com.amory.musicapp.managers.UriAudioManger
import com.amory.musicapp.model.Track
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
        binding.songNameTXT.isSelected = true
        binding.imvPlay.setOnClickListener {
            if (PlayMusicActivity.isPlayingMusic) {
                pauseMusic()
            } else {
                playMusic()
            }
        }
        binding.nextBtn.setOnClickListener {
            onClickNextSong()
        }
        // Handle click on the Now Playing fragment to open PlayMusicActivity
        binding.root.setOnClickListener {
            openPlayMusicActivity()
        }
        return binding.root
    }

    private fun onClickNextSong() {
        setSongPosition(true)
        val listTracks = PlayMusicActivity.listTrack
        val positionTrack = PlayMusicActivity.positionTrack
        listTracks?.get(positionTrack)?.let {
            UriAudioManger.getUriAudio(it) { uriAudio ->
                uriAudio?.let { uri ->
                    PlayMusicActivity.musicService?.let { service ->
                        service.mediaPlayer?.apply {
                            reset()
                            setDataSource(uri)
                            prepareAsync()
                        }
                    }
                }
            }
        }
        PlayMusicActivity.musicService?.showNotification(R.drawable.ic_pause_now)
        setLayout(listTracks,positionTrack)
        playMusic()
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
    private fun setLayout(listTrack : MutableList<Track>?, positionTrack : Int){
        listTrack.let { tracks ->
            tracks!![positionTrack].let {
                binding.nameArtistTXT.text = it.artists.joinToString(", ") { artist -> artist.name }
                binding.songNameTXT.text = it.name
                Glide.with(binding.root).load(it.thumbnail).into(binding.imvTrack)
            }
        }
    }

    private fun openPlayMusicActivity() {
        val intent = Intent(requireContext(), PlayMusicActivity::class.java).apply {
            putExtra("positionTrack", PlayMusicActivity.positionTrack)
            putExtra("class","NowPlaying")
            putExtra("currentPosition", PlayMusicActivity.musicService?.mediaPlayer?.currentPosition)
        }
        ContextCompat.startActivity(requireContext(),intent,null)
    }
}
