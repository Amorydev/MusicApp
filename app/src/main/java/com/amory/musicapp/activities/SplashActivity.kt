package com.amory.musicapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.amory.musicapp.MainActivity
import com.amory.musicapp.R
import com.amory.musicapp.config.AuthStateManager
import com.amory.musicapp.databinding.ActivitySplashBinding
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.DoubleBounce

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var mAuthStateManager: AuthStateManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        mAuthStateManager = AuthStateManager.getInstance(this)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        Handler().postDelayed(
            {
               binding.spinKit.isIndeterminate = false
                if (mAuthStateManager.getCurrent().isAuthorized) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }else {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }, 2000
        )
    }
}