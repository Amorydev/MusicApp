package com.amory.musicapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amory.musicapp.Interface.OnClickBtnAddMusicInPlaylist
import com.amory.musicapp.databinding.LayoutRecyclerviewAddItemPlaylistBinding
import com.amory.musicapp.model.Track
import com.bumptech.glide.Glide

class AddItemInPlaylistAdapter(private val listTracks : List<Track?>, val onClickBtnAddMusicInPlaylist: OnClickBtnAddMusicInPlaylist):RecyclerView.Adapter<AddItemInPlaylistAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: LayoutRecyclerviewAddItemPlaylistBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(data: Track){
            binding.txtSongName.text = data.name
            Glide.with(binding.root).load(data.thumbnail).into(binding.imvTrackImage)
            binding.addItemBtn.setOnClickListener {
                onClickBtnAddMusicInPlaylist.onClickBtnAddMusicInPlaylist(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutRecyclerviewAddItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listTracks[position]!!)
    }

    override fun getItemCount(): Int {
        return listTracks.size
    }
}