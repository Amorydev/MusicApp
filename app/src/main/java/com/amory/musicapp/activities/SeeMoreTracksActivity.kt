package com.amory.musicapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.amory.musicapp.R
import com.amory.musicapp.adapter.SeeMoreTrackAdapter
import com.amory.musicapp.databinding.ActivitySeeMoreTracksBinding
import com.amory.musicapp.model.Track
import com.amory.musicapp.viewModel.SeeMoreTrackViewModel

class SeeMoreTracksActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeeMoreTracksBinding
    private val seeMoreTrackViewModel: SeeMoreTrackViewModel by viewModels<SeeMoreTrackViewModel>()
    private var page: Int = 1
    private lateinit var adapterSeeMoreTrack: SeeMoreTrackAdapter
    private val listTracks : MutableList<Track> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeeMoreTracksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapterSeeMoreTrack = SeeMoreTrackAdapter(listTracks)
        seeMoreTrackViewModel.getTrack(page, 20)

        seeMoreTrackViewModel.listTrack.observe(this) { tracks ->
            adapterSeeMoreTrack.addData(tracks as MutableList<Track>)
            setRecyclerView()
        }

        binding.tracksRv.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layout = binding.tracksRv.layoutManager as LinearLayoutManager
                val totalItem = layout.itemCount
                val lastItem = layout.findLastVisibleItemPosition()

                if (!adapterSeeMoreTrack.showSeeMoreButton && totalItem <= lastItem + 2) {
                    adapterSeeMoreTrack.showSeeMore()
                }
            }
        })
        adapterSeeMoreTrack.setOnSeeMoreClickListener {
            loadMoreData()
            /*Toast.makeText(this, "SeeMore Click", Toast.LENGTH_SHORT).show()*/
        }
    }
    private fun loadMoreData(){
        page++
        seeMoreTrackViewModel.getTrack(page, 20)
        seeMoreTrackViewModel.listTrack.observe(this){ tracks ->
            adapterSeeMoreTrack.addData(tracks as MutableList<Track>)
            setRecyclerView()
        }

    }
    private fun setRecyclerView(){
        binding.tracksRv.adapter = adapterSeeMoreTrack
        binding.tracksRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.tracksRv.setHasFixedSize(true)
    }

}