package com.amory.musicapp.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amory.musicapp.R
import com.amory.musicapp.databinding.FragmentProfileBinding
import com.amory.musicapp.model.AuthResponse
import com.amory.musicapp.retrofit.APICallAuth
import com.amory.musicapp.retrofit.RetrofitClient
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding ?= null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAccount()
    }

    private fun getAccount() {
        val service = RetrofitClient.retrofitInstance.create(APICallAuth::class.java)
        val callAccount = service.getAccount()
        callAccount.enqueue(object : Callback<AuthResponse>{
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful){
                    if (_binding !=null){
                        initView(response.body())
                    }
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {

            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initView(body: AuthResponse?) {
        binding.nameAccountTXT.text = body?.firstName +" "+ body?.lastName
        Glide.with(binding.root).load(body?.picture).into(binding.imvAccount)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}