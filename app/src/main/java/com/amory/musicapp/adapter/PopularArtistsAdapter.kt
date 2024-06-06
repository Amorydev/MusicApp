package com.amory.musicapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amory.musicapp.databinding.LayoutRecyclerciewPopularArtistBinding
import com.amory.musicapp.model.Artists
import com.bumptech.glide.Glide

class PopularArtistsAdapter(private val listArtists:MutableList<Artists>):RecyclerView.Adapter<PopularArtistsAdapter.viewHolder>() {
    inner class viewHolder(private val binding:LayoutRecyclerciewPopularArtistBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(data:Artists){
            binding.nameArtist.text = data.name
            Glide.with(binding.root).load(data.thumbnail).into(binding.imvPopularArtist)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutRecyclerciewPopularArtistBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return viewHolder(view)
    }

    override fun getItemCount(): Int {
        return listArtists.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        return holder.bind(listArtists[position])
    }
}