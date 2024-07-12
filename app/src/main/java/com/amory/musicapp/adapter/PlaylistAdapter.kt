package com.amory.musicapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amory.musicapp.Interface.OnclickItemPlaylist
import com.amory.musicapp.databinding.LayoutRecyclerviewItemPlaylistBinding
import com.amory.musicapp.model.Playlist
import com.bumptech.glide.Glide

class PlaylistAdapter(private val listPlaylist: MutableList<Playlist>, private val onClickItemPlaylist: OnclickItemPlaylist) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: LayoutRecyclerviewItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Playlist) {
            binding.namePlaylistTxt.text = data.name
            binding.ArtistPlaylistTxt.text = data.ownerId
            Glide.with(binding.root).load(data.thumbnail).into(binding.playlistImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutRecyclerviewItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listPlaylist[position])
        holder.itemView.setOnClickListener {
            onClickItemPlaylist.onClickItemPlaylist(position)
        }
    }

    override fun getItemCount(): Int {
        return listPlaylist.size
    }
}