package com.amory.musicapp.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.amory.musicapp.Interface.OnclickItemPlaylist
import com.amory.musicapp.R
import com.amory.musicapp.adapter.PlaylistAdapter
import com.amory.musicapp.databinding.FragmentLibraryBinding
import com.amory.musicapp.managers.PlaylistManager
import com.amory.musicapp.model.Playlist

class LibraryFragment : Fragment() {
    private var _binding : FragmentLibraryBinding ?= null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.AddPlaylistCV.setOnClickListener {
            val fragment = AddPlaylistFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.AddPlaylistTxt.setOnClickListener {
            val fragment = AddPlaylistFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        setUpLayout()
    }
    private fun setUpLayout(){
        PlaylistManager.getPlaylist(1,10){ listPlaylist ->
            setupRvItemPlaylist(listPlaylist)
        }
    }

    private fun setupRvItemPlaylist(listPlaylist: MutableList<Playlist>) {
        val adapter = PlaylistAdapter(listPlaylist, object : OnclickItemPlaylist{
            override fun onClickItemPlaylist(position: Int) {
                val fragment = DetailPlaylistFragment()

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        })
        binding.playlistRV.adapter = adapter
        binding.playlistRV.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        binding.playlistRV.setHasFixedSize(true)
    }

}