package com.amory.musicapp.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.amory.musicapp.managers.PlaylistManager
import com.amory.musicapp.model.DetailPlaylistResponse
import com.amory.musicapp.model.Playlist
import com.amory.musicapp.model.Track
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DetailPlaylistViewModel : ViewModel() {

    private val _name = MutableLiveData<String?>()
    val name: LiveData<String?> get() = _name

    private val _items = MutableLiveData<List<Track>>()
    val items: LiveData<List<Track>> get() = _items

    private val _isPublic = MutableLiveData<Boolean?>()
    val isPublic : LiveData<Boolean?> get() = _isPublic

    private val _nameArtist = MutableLiveData<String?>()
    val nameArtist: LiveData<String?> get() = _nameArtist


    fun setName(value: String) {
        _name.value = value
    }

     fun fetchPlaylistDetails() {
        viewModelScope.launch {
            try {
                val playlists = getAllPlaylists(1, 10)
                playlists?.find { it.name == _name.value }?.let { playlist ->
                    val fetchedPlaylist = getPlaylistById(playlist.id)
                    _items.value = fetchedPlaylist?.items
                    _isPublic.value = fetchedPlaylist?.isPublic
                    val fullNameArtist = fetchedPlaylist!!.owner.firstName + fetchedPlaylist.owner.lastName
                    _nameArtist.value = fullNameArtist
                }
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun getAllPlaylists(page: Int, size: Int): List<Playlist>? =
        suspendCancellableCoroutine { continuation ->
            PlaylistManager.getAllPlaylist(page, size) { playlists ->
                continuation.resume(playlists)
            }
        }

    private suspend fun getPlaylistById(id: String): DetailPlaylistResponse? =
        suspendCancellableCoroutine { continuation ->
            PlaylistManager.getPlaylistById(id) { playlist ->
                if (playlist != null) {
                    continuation.resume(playlist)
                } else {
                    continuation.resumeWithException(Exception("Failed to fetch playlist by ID"))
                }
            }
        }
}
