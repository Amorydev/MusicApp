package com.amory.musicapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amory.musicapp.managers.PlaylistManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class AddPlaylistViewModel : ViewModel() {

    private val _addPlaylistResult = MutableLiveData<Boolean?>()
    val addPlaylistResult: LiveData<Boolean?> get() = _addPlaylistResult

    fun addPlaylist(name: String, isPublic: Boolean,description:String,thumbnail:String  ) {
        val namePlaylist = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
        val isPublicPlaylist = RequestBody.create("text/plain".toMediaTypeOrNull(), isPublic.toString())
        val descriptionPlaylist = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
        val thumbnailPlaylist = RequestBody.create("text/plain".toMediaTypeOrNull(), thumbnail)

        PlaylistManager.addPlaylist(namePlaylist, isPublicPlaylist, thumbnailPlaylist, descriptionPlaylist) { isSuccess ->
            _addPlaylistResult.postValue(isSuccess)
        }
    }
}