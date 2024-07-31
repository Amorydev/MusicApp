package com.amory.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.amory.musicapp.R
import com.amory.musicapp.activities.PlayMusicActivity
import com.amory.musicapp.databinding.FragmentNowPlayingBinding
import com.amory.musicapp.viewModel.NowPlayingViewModel
import com.bumptech.glide.Glide

class NowPlayingFragment : Fragment() {
    private var _binding: FragmentNowPlayingBinding? = null
    private val binding get() = _binding!!

    private val nowPlayingViewModel: NowPlayingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        nowPlayingViewModel.init()
        initialViews()
        setClick()
        return binding.root
    }

    private fun setClick() {
        binding.imvPlay.setOnClickListener { nowPlayingViewModel.playOrPauseMusic() }
        binding.nextBtn.setOnClickListener { nowPlayingViewModel.nextSong() }
        binding.likeNowBTN.setOnClickListener { nowPlayingViewModel.toggleLike() }
        binding.root.visibility = View.INVISIBLE

        binding.root.setOnClickListener {
            openPlayMusicActivity()
        }
    }

    private fun initialViews() {
        nowPlayingViewModel.currentTracks.observe(viewLifecycleOwner) { track ->
            binding.songNameTXT.text = track?.name
            binding.nameArtistTXT.text = track?.artists?.joinToString(", ") { it.name }
            Glide.with(binding.root).load(track?.thumbnail).into(binding.imvTrack)
        }
        PlayMusicActivity._isPlaying?.let { nowPlayingViewModel.updateIsPlaying(it) }
        Log.d("like", PlayMusicActivity.likeSend.toString())
        PlayMusicActivity.likeSend?.let { nowPlayingViewModel.updateLike(it) }
        nowPlayingViewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            Log.d("isPlayingNow", isPlaying.toString())
            binding.imvPlay.setImageResource(
                if (isPlaying) R.drawable.ic_pause_now else R.drawable.ic_play_now
            )
            PlayMusicActivity._isPlaying = isPlaying
        }
        nowPlayingViewModel.backgroundGradient.observe(viewLifecycleOwner) {
            binding.CardView.setCardBackgroundColor(it!!)
        }

        nowPlayingViewModel.isLike.observe(viewLifecycleOwner){like->
            like?.let {
                if (it) binding.likeNowBTN.setImageResource(R.drawable.ic_love) else binding.likeNowBTN.setImageResource(R.drawable.ic_no_love)
            }
            PlayMusicActivity.likeSend = like
        }
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
