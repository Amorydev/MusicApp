package com.amory.musicapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amory.musicapp.Interface.OnCLickTrack
import com.amory.musicapp.databinding.LayoutRecyclerviewPopularTrackBinding
import com.amory.musicapp.databinding.LayoutRecyclerviewSearchTrackBinding
import com.amory.musicapp.model.Track
import com.bumptech.glide.Glide

class SearchTrackAdapter(private val listTrack: MutableList<Track>, private val onCLickTrack: OnCLickTrack) : RecyclerView.Adapter<SearchTrackAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: LayoutRecyclerviewSearchTrackBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Track) {
            binding.txtSongName.text = data.name
            binding.txtSongDuration.text = formatSecondsToMinutesAndSeconds(data.durationSec)
            if (data.artists.isNotEmpty() && adapterPosition < data.artists.size) {
                binding.txtArtists.text = data.artists[adapterPosition].name
            }
            Glide.with(binding.root).load(data.thumbnail).into(binding.imvTrackImage)
        }
    }
    fun formatSecondsToMinutesAndSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutRecyclerviewSearchTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = listTrack.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listTrack[position])
        holder.itemView.setOnClickListener {
            onCLickTrack.onCLickTrack(position)
        }
    }
}
