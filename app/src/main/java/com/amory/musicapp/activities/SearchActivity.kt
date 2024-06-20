package com.amory.musicapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.amory.musicapp.R
import com.amory.musicapp.databinding.ActivitySearchBinding
import com.amory.musicapp.model.Artists
import com.amory.musicapp.model.Track

class SearchActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchBinding
    private var listArtist:MutableList<Artists> ?= null
    private var listTrack:MutableList<Track> ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onSearch()
    }

    private fun onSearch() {
        binding.searchET.addTextChangedListener(object  : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 0){
                    listArtist!!.clear()
                    listTrack!!.clear()
                }else{
                    search(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun search(search:String) {
        listArtist!!.clear()
        listTrack!!.clear()
    }
}