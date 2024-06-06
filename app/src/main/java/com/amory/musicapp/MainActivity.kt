package com.amory.musicapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.amory.musicapp.databinding.ActivityMainBinding
import com.amory.musicapp.fragment.HomeFragment
import com.amory.musicapp.fragment.LibraryFragment
import com.amory.musicapp.fragment.ProfileFragment
import com.amory.musicapp.model.TokenResponse
import com.amory.musicapp.retrofit.APICallToken
import com.amory.musicapp.retrofit.RetrofitClient
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = this.getSharedPreferences("SAVE_TOKEN", Context.MODE_PRIVATE)
        RetrofitClient.init(this)
        getToken()
        initViews()
        getToken()
    }

    private fun initViews() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.library -> {
                    replaceFragment(LibraryFragment())
                    true
                }

                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                else -> {
                    replaceFragment(HomeFragment())
                    true
                }

            }

        }
        //Fragment default
        replaceFragment(HomeFragment())
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun getToken() {
        val service = RetrofitClient.retrofitInstance.create(APICallToken::class.java)
        val callToken = service.getToken()
        callToken.enqueue(object : Callback<TokenResponse> {
            @SuppressLint("CommitPrefEdits")
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    Log.d("token", token.toString())
                    val editor = sharedPreferences.edit()
                    editor.putString("token", token)
                    editor.apply()
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Log.d("token", t.message.toString())
            }
        })
    }

    override fun onStart() {
        super.onStart()
        getToken()
    }

}