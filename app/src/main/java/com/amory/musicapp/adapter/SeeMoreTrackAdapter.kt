package com.amory.musicapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.amory.musicapp.R
import com.amory.musicapp.model.Track
import com.bumptech.glide.Glide

class SeeMoreTrackAdapter(private val listTracks: MutableList<Track>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mcontext: Context
    var showSeeMoreButton: Boolean = false

    companion object {
        const val VIEW_TYPE = 1
        const val VIEW_SEE_MORE = 2
    }

    inner class TracksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTrack: TextView = itemView.findViewById(R.id.txt_songName)
        val songDuration: TextView = itemView.findViewById(R.id.txt_songDuration)
        val artists: TextView = itemView.findViewById(R.id.txt_artists)
        val imvTrack: ImageView = itemView.findViewById(R.id.imv_trackImage)
    }

    inner class SeeMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val button: Button = itemView.findViewById(R.id.seeMoreBTN)

        fun bind() {
            button.setOnClickListener {
                onSeeMoreClickListener?.invoke()
                /*Toast.makeText(mcontext,"SeeMore Click", Toast.LENGTH_SHORT).show()*/
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(value: MutableList<Track>) {
        val startPosition = listTracks.size
        this.listTracks.addAll(value)
        notifyItemRangeInserted(startPosition, value.size)
    }

    fun showSeeMore() {
        if (!showSeeMoreButton) {
            showSeeMoreButton = true
            notifyItemInserted(listTracks.size)
        }
    }

    fun hintSeeMore() {
        if (showSeeMoreButton) {
            showSeeMoreButton = false
            notifyItemRemoved(listTracks.size)
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatSecondsToMinutesAndSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun getItemCount(): Int {
        return listTracks.size + if (showSeeMoreButton) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == listTracks.size) VIEW_SEE_MORE else VIEW_TYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE) {
            val trackHolder = holder as TracksViewHolder
            trackHolder.nameTrack.text = listTracks[position].name
            trackHolder.songDuration.text =
                formatSecondsToMinutesAndSeconds(listTracks[position].durationSec)
            val artist = listTracks[position].artists.joinToString(",") { it -> it.name }
            trackHolder.artists.text = artist
            Glide.with(mcontext).load(listTracks[position].thumbnail).into(trackHolder.imvTrack)
        } else {
            val seeMoreHolder = holder as SeeMoreViewHolder
            seeMoreHolder.bind()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mcontext = parent.context
        return if (viewType == VIEW_TYPE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_recyclerview_popular_track, parent, false)
            TracksViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.layout_see_more, parent, false)
            SeeMoreViewHolder(view)
        }
    }

    private var onSeeMoreClickListener: (() -> Unit)? = null

    fun setOnSeeMoreClickListener(listener: () -> Unit) {
        onSeeMoreClickListener = listener
    }
}