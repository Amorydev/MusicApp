package com.amory.musicapp.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.amory.musicapp.R
import com.amory.musicapp.databinding.FragmentAddPlaylistBinding
import com.amory.musicapp.managers.PlaylistManager.addPlaylist
import com.amory.musicapp.viewModel.AddPlaylistViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody


class AddPlaylistFragment : Fragment() {
    private var _binding: FragmentAddPlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddPlaylistViewModel by viewModels<AddPlaylistViewModel>()
    private lateinit var name: String
    private var isPublic: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClickAddPlaylist()
        observer()
    }

    private fun observer() {
        viewModel.addPlaylistResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess == true) {
                Toast.makeText(requireContext(), "Add playlist success", Toast.LENGTH_SHORT)
                    .show()
                val fragment = DetailPlaylistFragment()

                val bundle = Bundle()
                bundle.putString("namePlaylist", name)
                bundle.putBoolean("isPublic", isPublic)
                fragment.arguments = bundle

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun onClickAddPlaylist() {
        binding.addPlaylistBtn.setOnClickListener {
            name = binding.namePlaylistET.text.trim().toString()
            isPublic = binding.isPublicSW.isChecked
            viewModel.addPlaylist(name, isPublic, "", "")
        }
    }


}