package com.amory.musicapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amory.musicapp.managers.PlaylistManager

class DetailPlaylistViewModel : ViewModel() {
    private val _name = MutableLiveData<String?>()
    private val _id = MutableLiveData<String?>()

    fun setName(value: String) {
        _name.value = value
    }

    fun getId() {
        PlaylistManager.getAllPlaylist(1, 10) { playlist ->
            for (i in playlist) {
                if (i.name == _name.value) {
                    PlaylistManager.getPlaylistById(i.id){

                    }
                }
            }
        }
    }

}