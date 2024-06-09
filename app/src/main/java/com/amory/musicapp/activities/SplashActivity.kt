package com.amory.musicapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.amory.musicapp.MainActivity
import com.amory.musicapp.R
import com.amory.musicapp.databinding.ActivitySplashBinding
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.DoubleBounce

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        Handler().postDelayed(
            {
               binding.spinKit.isIndeterminate = false
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, 2000
        )
    }
}