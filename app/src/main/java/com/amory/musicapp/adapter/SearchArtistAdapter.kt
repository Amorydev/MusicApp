package com.amory.musicapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amory.musicapp.Interface.OnCLickArtist
import com.amory.musicapp.databinding.LayoutRecyclerviewSearchArtistBinding
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.eventBus.EventPostIdsArtist
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.EventBus

class SearchArtistAdapter(private val listArtists:List<Artists>, private val onCLickArtist: OnCLickArtist):
    RecyclerView.Adapter<SearchArtistAdapter.viewHolder>() {
    inner class viewHolder(private val binding: LayoutRecyclerviewSearchArtistBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(data: Artists){
            binding.nameArtist.text = data.name
            Glide.with(binding.root).load(data.thumbnail).centerCrop().into(binding.imvPopularArtist)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutRecyclerviewSearchArtistBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return viewHolder(view)
    }

    override fun getItemCount(): Int {
        return listArtists.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(listArtists[position])
        holder.itemView.setOnClickListener {
            onCLickArtist.onCLickArtist(position)
            EventBus.getDefault().post(EventPostIdsArtist(listArtists[position]))
        }
    }
}