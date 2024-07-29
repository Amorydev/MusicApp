package com.amory.musicapp.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.amory.musicapp.R
import com.amory.musicapp.databinding.FragmentDetailPlaylistBinding
import com.amory.musicapp.viewModel.DetailPlaylistViewModel

class DetailPlaylistFragment : Fragment() {
    private var _binding: FragmentDetailPlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailPlaylistViewModel by viewModels<DetailPlaylistViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val name = arguments?.getString("namePlaylist")
        if (name != null) {
            viewModel.setName(name)
            viewModel.fetchPlaylistDetails()
            observer()
        } else {
            Toast.makeText(requireContext(), "Playlist name is null", Toast.LENGTH_SHORT).show()
            Log.e("DetailPlaylistFragment", "Playlist name is null")
        }
        onCLickAddMusic()
    }

    private fun onCLickAddMusic() {
        binding.addMusicBtn.setOnClickListener {
            val fragment = AddItemInPlaylistFragment()

            val bundle = Bundle()
            viewModel.id.observe(viewLifecycleOwner){id->
                bundle.putString("id",id)
            }
            fragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_container,fragment)
                .commit()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observer() {
        viewModel.name.observe(viewLifecycleOwner) {
            binding.namePlaylistTxt.text = it
        }
        viewModel.items.observe(viewLifecycleOwner) { items ->
            Log.d("DetailPlaylistFragment", "Number of items: ${items.size}")
            binding.totalItemPlaylistTxt.text = "${items.size} bài hát"
           /* if (items.isNotEmpty()){
                binding.addMusicBtn.visibility = View.VISIBLE
            }else{
                binding.addMusicBtn.visibility = View.INVISIBLE
            }*/
        }
        viewModel.isPublic.observe(viewLifecycleOwner) { isPublic ->
            if (isPublic == true) binding.isPublicImv.setImageResource(R.drawable.ic_profile) else binding.isPublicImv.setImageResource(
                R.drawable.ic_public
            )
        }
        viewModel.nameArtist.observe(viewLifecycleOwner){fullName ->
            binding.nameArtistPlaylist.text = fullName
        }
    }
}
