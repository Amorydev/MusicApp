package com.amory.musicapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amory.musicapp.R
import com.amory.musicapp.databinding.ActivitySeeMoreTracksBinding

class SeeMoreTracksActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeeMoreTracksBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeeMoreTracksBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}